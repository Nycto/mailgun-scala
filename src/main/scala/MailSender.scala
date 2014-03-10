package com.roundeights.mailgun

import com.roundeights.scalon.{nObject, nParser}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import dispatch._

/** @see MailSender */
object MailSender {

    /** The result of a piece of sent mail */
    case class Response( val id: String, val message: String ) {

        /** Builds a response from a JSON object */
        def this ( obj: nObject ) = this( obj.str("id"), obj.str("message") )
    }

    /** A dummy mail sender */
    class Dummy extends MailSender {

        /** {@inheritDoc} */
        override def send ( email: Email ): Future[MailSender.Response] = {
            println( email )
            Future.successful(Response(UUID.randomUUID.toString, "Dummy Send"))
        }

        /** {@inheritDoc} */
        override def toString = "DummyMailSender"
    }
}

/** An interface for sending emails */
trait MailSender {

    /** Sends the given email and returns a response */
    def send ( email: Email ): Future[MailSender.Response]

    /** Sends the given email and returns a response */
    def send(
        to: String, from: String, subject: String, body: String
    ): Future[MailSender.Response] = send( new Email(to, from, subject, body) )

    /** Sends the given email and returns a response */
    def send(
        to: Email.Addr, from: Email.Addr, subject: String, body: Email.Body
    ): Future[MailSender.Response] = send( Email(to, from, subject, body) )
}

/** A mail sender for Mailgun */
class Mailgun
    ( server: String, apiKey: String )
    ( implicit val ctx: ExecutionContext )
extends MailSender {

    /** The mailgun API url */
    private val url = "https://api.mailgun.net/v2/" + server + "/messages"
    //private val url = "http://requestb.in/"

    /** A list of headers to send along with each request */
    private val headers = {
        val value = "api:" + apiKey
        val encoded = new sun.misc.BASE64Encoder().encode( value.getBytes )
        Map("Authorization" -> ("Basic " + encoded))
    }

    /** {@inheritDoc} */
    override def send ( email: Email ): Future[MailSender.Response] = {
        val request = dispatch.url( url ) << email.toMap <:< headers
        Http( request.OK(as.String) )
            .map( nParser.jsonObj _ )
            .map( new MailSender.Response(_) )
    }

    /** {@inheritDoc} */
    override def toString = "MailgunSender(%s)".format( server )
}


