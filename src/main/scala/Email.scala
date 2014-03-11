package com.roundeights.mailgun

/** @see Email */
object Email {

    /** @see Addr */
    object Addr {

        /** Alternate constructor */
        def apply( address: String, name: String ): Addr
            = new Addr(address, Some(name))
    }

    /** An email address */
    case class Addr ( val address: String, val name: Option[String] = None ) {

        /** Returns this email address encoded as a string */
        def encode: String = name match {
            case Some(innerName) => "%s <%s>".format(innerName, address)
            case None => address
        }
    }

    /** Email content */
    trait Body {

        /** Returns this body as a map */
        def toMap: Map[String, String]
    }

    /** Text content */
    trait TextBody extends Body

    /** HTML Content */
    trait HtmlBody extends Body


    /** Uses text for the body of an email */
    def text( content: String ): TextBody = new TextBody {
        override def toMap = Map( "text" -> content )
        override def toString = "Text(%s)".format(content)
    }

    /** Uses html for the body of an email */
    def html( content: String ): HtmlBody = new HtmlBody {
        override def toMap = Map( "html" -> content )
        override def toString = "Html(%s)".format(content)
    }

    /** Uses text and html for the body of an email */
    def textAndHtml(
        textContent: String,
        htmlContent: String
    ): TextBody with HtmlBody = new TextBody with HtmlBody {
        override def toMap = Map("text" -> textContent, "html" -> htmlContent)
        override def toString =
            "Html(%s) Text(%s)".format(textContent, htmlContent)
    }
}

/** A specific email to be sent */
case class Email (
    private val to: Email.Addr,
    private val from: Email.Addr,
    private val subject: String,
    private val body: Email.Body,
    private val tag: Option[String] = None
) {

    /** Construct from strings */
    def this( to: String, from: String, subject: String, body: String ) = this(
        Email.Addr(to, None), Email.Addr(from, None),
        subject, Email.html(body) )

    /** Converts this instance to a Map */
    def toMap: Map[String, String] = {
        Map("to" -> to.encode, "from" -> from.encode, "subject" -> subject) ++
            ( body.toMap ) ++
            ( if (tag.isDefined) Map("tag" -> tag.get) else Map() )
    }
}

