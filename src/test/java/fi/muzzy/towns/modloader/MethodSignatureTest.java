package fi.muzzy.towns.modloader;

import static org.junit.Assert.*;
import org.junit.Test;

public class MethodSignatureTest
{
	@Test public void testParsing()
    {
		MethodSignature sig = new MethodSignature("foo(bar,baz)");
		
        assertEquals("Testing signature name", "foo", sig.getName());
        assertArrayEquals("Testing signature params", new String[]{"bar","baz"}, sig.getParams());
	}

	@Test public void testEmptyParams()
    {
		MethodSignature sig = new MethodSignature("foo()");
		
        assertEquals("Testing signature name", "foo", sig.getName());
        assertArrayEquals("Testing signature params", new String[0], sig.getParams());

    }
}
