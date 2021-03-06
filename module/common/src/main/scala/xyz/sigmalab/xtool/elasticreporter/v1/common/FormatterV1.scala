package xyz.sigmalab.xtool.elasticreporter.v1.common

import xyz.sigmalab.xtool.elasticreporter.v1.common

object FormatterV1 {

    case class Value(key: String, value: String, tipe: common.Value.Type) extends common.Value

    def simple(key: String, value: String) = Value(key, value, common.Value.Simple)

    def qouted(key: String, value: String) = Value(key, value, common.Value.Qouted)

    case class FormatException(
        desc: String,
        rawKey: String,
        rawValue: String,
        cause: Option[Throwable]
    ) extends IllegalArgumentException(desc, cause.orNull) with data.BaseException

}

class FormatterV1 extends Formatter {

    import FormatterV1._

    type Val = FormatterV1.Value

    private val keyValidationRegex = "[\\|/\"\'\n\r]".r

    protected def isInvalidKey(key: String) =
        keyValidationRegex.findFirstIn(key).isDefined

    protected def checkInvalidKey(key: String, value: String) =
        if (isInvalidKey(key)) throw new FormatException(s"Invalid Key to Format: '${key}'", key, value, None)

    private val valueValidationRegex = "[\"\n\r]".r

    protected def inInvalidValue(value: String) =
        valueValidationRegex.findFirstIn(value).isDefined

    protected def checkInvalidStringValue(key: String, value: String) =
        if (inInvalidValue(value)) throw new FormatException(s"Invlaid Value to Format: '${value}'", key, value, None)

    override def formatString(rawKey : String, rawVal : String): Val  = {
        checkInvalidKey(rawKey, rawVal)
        checkInvalidStringValue(rawKey, rawVal)
        qouted(rawKey, rawVal)
    }

    override def formatBool(rawKey : String, rawVal : Boolean) : Val = {
        val value = rawVal match {
            case true => "true"
            case false => "false"
        }
        checkInvalidKey(rawKey, value)
        simple(rawKey, value)
    }

    override def formatInt(rawKey : String, rawVal : Int) : Val = {
        val value = rawVal.toString
        checkInvalidKey(rawKey, value)
        simple(rawKey, value)
    }

    override def formatLong(rawKey : String, rawVal : Long) : Val = {
        val value = rawVal.toString
        checkInvalidKey(rawKey, value)
        simple(rawKey, value)
    }

    override def formatFloat(rawKey : String, rawVal : Float) : Val = {

        // @todo Log Infinity occurences! or change function to return Option[Val]

        val rValue = rawVal match {
            case java.lang.Double.POSITIVE_INFINITY => 0
            case java.lang.Double.NEGATIVE_INFINITY => 0
            case other => other
        }

        val value = rValue.toString
        checkInvalidKey(rawKey, value)
        simple(rawKey, value)
    }

    override def formatDouble(rawKey : String, rawVal : Double) : Val = {

        // @todo Log Infinity occurences! or change function to return Option[Val]

        val rValue = rawVal match {
            case java.lang.Double.POSITIVE_INFINITY => 0
            case java.lang.Double.NEGATIVE_INFINITY => 0
            case other => other
        }

        val value = rValue.toString
        checkInvalidKey(rawKey, value)
        simple(rawKey, value)
    }

    protected def append(value: Val)(implicit buf: StringBuilder) = {
        buf.append('"').append(value.key).append("\": ")
        value.tipe match {
            case common.Value.Simple =>
                buf.append(value.value)
            case common.Value.Qouted =>
                buf.append('"').append(value.value).append('"')
        }
    }

    override def format(values : Seq[Val]) : String = {

        implicit val buf = new StringBuilder

        buf.append("{")

        values match {
            case Nil => // nothing
            case head :: Nil => append(head)
            case head :: tail=>
                append(head)
                tail.foreach { i =>
                    buf.append(',').append(' ')
                    append(i)
                }
        }

        buf.append("}").result()
    }
}
