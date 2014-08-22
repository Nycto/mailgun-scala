MailgunScala [![Build Status](https://secure.travis-ci.org/Nycto/mailgun-scala.png?branch=master)](http://travis-ci.org/Nycto/mailgun-scala)
============

A Scala wrapper around the Mailgun sending API. Their documentation can
be found here:

http://documentation.mailgun.com/api-sending.html

Example
-------

Start by instantiating a `MailSender` for your application:

```scala
import com.roundeights.mailgun.{MailSender, Mailgun}

lazy val sender: MailSender = {
    if ( System.getProperty("mailgun.isProd") != "true" ) {
        // In development, don't send actual mail
        new MailSender.Dummy
    }
    else {
        new Mailgun(
            System.getProperty("mailgun.server"),
            System.getProperty("mailgun.key")
        )
    }
}
```

Using it is then a single method call:

```scala
import com.roundeights.mailgun.Email
import scala.concurrent.Future
import scale.concurrent.ExecutionContext.Implicits.global

val response: Future[MailSender.Response] = sender.send(
    to = Email.Addr( "Joe User", "joe@example.com" ),
    from = Email.Addr( "Team Foo Bar", "contact@foobar.example.com" ),
    subject = "Welcome to Foo Bar!",
    body = Email.html( "Hey! Thanks for signing up." ),
    tag = Some("welcome")
)

// The response is a future containing the message and id of the sent email.
// The future will be failed if the send attempt failed
response.map(data => {
    println( data.id )
    println( data.message )
})
```

License
-------

This library is released under the MIT License, which is pretty spiffy. You
should have received a copy of the MIT License along with this program. If not,
see <http://www.opensource.org/licenses/mit-license.php>.

