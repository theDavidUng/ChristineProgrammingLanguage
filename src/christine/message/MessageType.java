package christine.message;

/**
 * <h1>MessageType</h1>
 *
 * <p>Message types.</p>
 *
 * <p>Copyright (c)
 * <p>2009 by Ronald Mak</p>
 * <p>2017 Updated Legacy Code: by David Ung, Christine Le, John Humlick, Alex Hsiao</p>
 *
 * <p>For instructional purposes only.  No warranties.</p>
 */
public enum MessageType
{
    SOURCE_LINE, SYNTAX_ERROR,
    PARSER_SUMMARY, INTERPRETER_SUMMARY, COMPILER_SUMMARY,
    MISCELLANEOUS, TOKEN,
    ASSIGN, FETCH, BREAKPOINT, RUNTIME_ERROR,
    CALL, RETURN,
}
