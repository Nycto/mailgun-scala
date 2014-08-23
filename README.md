MailgunScala [![Build Status](https://secure.travis-ci.org/Nycto/mailgun-scala.png?branch=master)](http://travis-ci.org/Nycto/mailgun-scala)
============

A Scala wrapper around the Mailgun sending API. Their documentation can
be found here:

http://documentation.mailgun.com/api-sending.html

Example
-------

The following example shows how to instantiate and send an email:

```scala
package testApp

import com.roundeights.mailgun.{MailSender, Mailgun, Email}

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

    // A MailSender coordinates the sending of email
    // Configure it with the server and api key that Mailgun provided on signup
    val sender: MailSender = new Mailgun(
        server = "samples.mailgun.org",
        apiKey = "key-3ax6xnjp29jd6fds4gc373sgvjxteol0"
    )

    // Send an email
    val response: Future[MailSender.Response] = sender.send( Email(
        to = Email.Addr("mailgun-scala@mailinator.com", "Joe User"),
        from = Email.Addr("nobody@example.com", "Test App"),
        subject = "Testing out Mailgun-Scala",
        body = Email.html("16 sodium atoms walk into a bar followed by Batman")
    ) )

    // The response is a future containing the message and id of the sent email.
    // The future will be failed if the send attempt failed
    try {
        Await.result(
            response.map(data => {
                println( data.id )
                println( data.message )
            }),
            3.seconds
        )
    }
    finally {
        // Shutdown the open connections
        sender.close
    }
}
```

If you want to use a Dummy mail sender outside of production, you can do
this instead:

```scala
// A dummy mail sender will always return a success response
val sender: MailSender = new MailSender.Dummy
```

License
-------

This library is released under the MIT License, which is pretty spiffy. You
should have received a copy of the MIT License along with this program. If not,
see <http://www.opensource.org/licenses/mit-license.php>.

