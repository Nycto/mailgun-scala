package com.roundeights.mailgun

import com.roundeights.scalon.{nObject, nParser, nException}
import com.ning.http.client.{AsyncHttpClientConfig, AsyncHttpClient}
import com.ning.http.client.{AsyncHandler, HttpResponseBodyPart}
import com.ning.http.client.{HttpResponseStatus, HttpResponseHeaders}
import com.ning.http.client.{Request, RequestBuilder}
import com.ning.http.client.AsyncHandler.STATE

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

/**
 * Makes REST requests.
 */
private class Requestor
    ( private val client: AsyncHttpClient )
    ( implicit context: ExecutionContext )
{

    /** Alternate constructor that puts together an async client */
    def this
        ( timeout: Int, maxConnections: Int )
        ( implicit context: ExecutionContext )
    = this(
        new AsyncHttpClient(
            new AsyncHttpClientConfig.Builder()
                .setCompressionEnabled(true)
                .setFollowRedirects(false)
                .setAllowPoolingConnection(true)
                .setRequestTimeoutInMs( timeout )
                .setMaximumConnectionsPerHost( maxConnections )
                .build()
        )
    )

    /** Shutsdown the underlying client */
    def close = client.close

    /** Executes a request */
    private def request ( req: Request ): Future[nObject] = {
        val async = new Asynchronizer
        client.executeRequest( req, async )
        async.future
    }

    /** Executes a request */
    def request (
       url: String,
       headers: Map[String, String],
       params: Map[String, String]
    ): Future[nObject] = {

        val builder = new RequestBuilder( "POST" ).setUrl( url )

        headers.foreach( pair => builder.addHeader( pair._1, pair._2 ) )

        params.foreach( pair => builder.addParameter( pair._1, pair._2 ) )

        request( builder.build )
    }
}

/**
 * The asynchronous request handler that accumulates a request as it is
 * receied.
 */
private class Asynchronizer extends AsyncHandler[Unit] {

    /** The promise that will contain the output of this request */
    private val result = Promise[nObject]()

    /** The error status, if one was encountered */
    private val status = new AtomicReference[Option[HttpResponseStatus]](None)

    /** Collects the body of the request as it is received */
    private val body = new StringBuffer

    /** Returns the future from this asynchronizer */
    def future = result.future

    /** {@inheritDoc} */
    override def onThrowable( t: Throwable ): Unit = result.failure(t)

    /** {@inheritDoc} */
    override def onBodyPartReceived(
        bodyPart: HttpResponseBodyPart
    ): STATE = {
        body.append( new String(bodyPart.getBodyPartBytes, "UTF-8") )
        STATE.CONTINUE
    }

    /** {@inheritDoc} */
    override def onStatusReceived(responseStatus: HttpResponseStatus): STATE = {
        // Store non-success responses, as they indicate an error
        if ( responseStatus.getStatusCode != 200 ) {
            status.set( Some(responseStatus) )
        }
        STATE.CONTINUE
    }

    /** {@inheritDoc} */
    override def onHeadersReceived( headers: HttpResponseHeaders ): STATE
        = STATE.CONTINUE

    /** {@inheritDoc} */
    override def onCompleted(): Unit = {
        status.get match {
            case None => result.complete( Try {
                nParser.jsonObj(body.toString)
            } )
            case Some(status) => {
                try {
                    val json = nParser.jsonObj(body.toString)
                    result.failure(new MailSender.Error(json.str("message")))
                }
                catch {
                    // If parsing the JSON fails, send a general request failure
                    case _: nException => result.failure(new MailSender.Error(
                        "%d: %s".format(
                            status.getStatusCode, status.getStatusText)))
                    case err: Throwable => result.failure(err)
                }
            }
        }
    }
}


