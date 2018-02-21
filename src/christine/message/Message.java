package christine.message;

/**
 * <h1>Message</h1>
 *
 * <p>Message format.</p>
 *
 * <p>Copyright (c)
 * <p>2009 by Ronald Mak</p>
 * <p>2017 Updated Legacy Code: by David Ung, Christine Le, John Humlick, Alex Hsiao</p>
 *
 * <p>For instructional purposes only.  No warranties.</p>
 */
public class Message
{
    private MessageType type;
    private Object body;

    /**
     * Constructor.
     * @param type the message type.
     * @param body the message body.
     */
    public Message(MessageType type, Object body)
    {
        this.type = type;
        this.body = body;
    }

    /**
     * Getter.
     * @return the message type.
     */
    public MessageType getType()
    {
        return type;
    }

    /**
     * Getter.
     * @return the message body.
     */
    public Object getBody()
    {
        return body;
    }
}
