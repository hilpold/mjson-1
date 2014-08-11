package testmjson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import mjson.Json;

/**
 * 
 * @author Borislav Iordanov
 *
 */
public class TestSchemas 
{
	public static byte[] getBytesFromStream(InputStream is, boolean close)
			throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			byte[] A = new byte[4096];
			// Read in the bytes
			for (int cnt = is.read(A); cnt > -1; cnt = is.read(A))
				out.write(A, 0, cnt);
			return out.toByteArray();
			// Close the input stream and return bytes
		}
		finally
		{
			if (close)
				is.close();
		}
	}
	
	public static String readFile(File file)
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream(file);				
			return new String(getBytesFromStream(in, true));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if (in != null) try { in.close(); } catch (Throwable t) { }
		}
	}
	
	public static String readTextResource(String resource)
	{
		InputStream in = TestSchemas.class.getResourceAsStream(resource);
		if (in == null)
			return null;
		else
			try
			{
				return new String(getBytesFromStream(in, true));
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
	}

	// this won't work for jar files probably, for test classes&resources are usually not bundled
	// in jars...
	public static Map<String, String> testResources(String name)
	{
		HashMap<String, String> L = new HashMap<String, String>();
		try
		{
			Enumeration<URL> en = TestSchemas.class.getClassLoader().getResources(name);
			if (en.hasMoreElements()) 
			{
			    URL resource = en.nextElement();
			    File resourceFile = new File(resource.toURI());
			    if (resourceFile.isDirectory())
			    	for (String nested : resourceFile.list())
			    	{
			    		File nestedFile = new File(resourceFile, nested);
			    		if (nestedFile.isDirectory())
			    			L.putAll(testResources(nested));
			    		else
			    			L.put(nestedFile.getPath(), readFile(nestedFile));
			    	}
			    else
			    	L.put(resourceFile.getPath(), readFile(resourceFile));
//			    File[] files = resourceFile.listFiles();
//			    String[] filenames = fileMetaInf.list();
			}
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
		return L;
	}
	
	/**
	 * Test a schema against a document that should validate and then
	 * against a document that should fail to validate.
	 * @param schema 
	 * @param correct 
	 * @param incorrect
	 */
	public void doTest(Json schema, Json correct, Json incorrect)
	{
		doTest(Json.schema(schema), correct, incorrect);
	}

	/**
	 * Test a schema against a document that should validate and then
	 * against a document that should fail to validate.
	 * @param schema 
	 * @param correct 
	 * @param incorrect
	 */
	public void doTest(Json.Schema schema, Json correct, Json incorrect)
	{
		Json ok = Json.nil();
		if (correct != null)
		{
			ok = schema.validate(correct);
			Assert.assertTrue(ok.at("errors", "").toString(), ok.at("ok").asBoolean());			
		}
		
		if (incorrect != null)
		{
			ok = schema.validate(incorrect);
			Assert.assertFalse(ok.at("ok").asBoolean());
		}
	}
	
	@Test
	public void testType()
	{
		doTest(Json.object("type", "string"), Json.make("asdfasd"), Json.array());
		doTest(Json.object("type", "array"), Json.array(), Json.make("asdfasd"));		
		doTest(Json.object("type", "object"), Json.object("asdf",23423), Json.nil());
		doTest(Json.object("type", "boolean"), Json.make(true), Json.object("asdf",23423));
		doTest(Json.object("type", "null"), Json.nil(), Json.make(false));
		doTest(Json.object("type", "number"), Json.make(23423.5345), Json.make("gdfgsdf"));		
		doTest(Json.object("type", "integer"), Json.make(5345), Json.make(5345.534));		
		doTest(Json.object("type", "any"), Json.make(5345), null);		
		doTest(Json.object("type", "any"), Json.make("Gasgfdsf"), null);
		doTest(Json.object("type", "any"), Json.array(), null);
	}
	
	@Test
	public void testEnum()
	{
		doTest(Json.object("enum", Json.array(null, 42, "hi", Json.object("a", 10))),
			   Json.make(42), Json.make("blabla"));
		doTest(Json.object("enum", Json.array(null, 42, "hi", Json.object("a", 10))),
				   Json.object().set("a",  10), Json.array(10));
		doTest(Json.object("enum", Json.array(null, 42, "hi", Json.object("a", 10))),
				   Json.make("hi"), Json.object("a", "hi"));
	}
	
	@Test
	public void testAllOf()
	{
		
	}
	
	@Test
	public void testAnyOf()
	{
		
	}
	
	@Test
	public void testOneOf()
	{
		
	}
	
	@Test
	public void testNot()
	{
		
	}
	
	@Test
	public void testDefinitions()
	{
		// this basic $ref testing as the implementation is not
		// required to do anything special with the 'definitions' keyword
	}
	
	@Test
	public void testInternalReference()
	{
		
	}
	
	@Test
	public void testExternalReference()
	{
		
	}
	
	@Test void testObjectProperties()
	{
		
	}
	
	@Test void testPatternProperties()
	{
		
	}
	
	@Test void testAdditionalProperties()
	{
		
	}
	
	@Test void testMaxMinProperties()
	{
		
	}
	
	@Test
	public void testRequiredProperties()
	{

		Json addr = Json.object();
		addr.set("region", "Saint Glin-glin");
		addr.set("locality", "Quebec");
		addr.set("country-name", "Canada");
		addr.set("extended-address", "22 Acatia Avenue");
		addr.set("street-address", "adfasdf");
		Json ok = addressSchema.validate(addr);
		Assert.assertTrue(ok.at("errors", "").toString(), ok.at("ok").asBoolean());
	}

	@Test
	public void testItems()
	{
		
	}
	
	@Test
	public void testAdditionalItems()
	{
		
	}
	
	@Test
	public void testUniqueItems()
	{
		
	}
	
	@Test
	public void testMinMaxItems()
	{
		
	}
	
	@Test
	public void testNumeric()
	{
		
	}
	
	@Test
	public void testString()
	{
		
	}
	
	private Json.Schema addressSchema;
	
	@BeforeClass
	public void setUp()
	{
		addressSchema = Json.schema(Json.read(readTextResource("/testmjson/address_schema.json")));		
	}
	
	
	@Factory
	public Object[] addTests()
	{
		List<SuiteTestJson> tests = new ArrayList<SuiteTestJson>(); 
		for (Map.Entry<String, String> test : testResources("suite").entrySet())
		{
			Json set = Json.read(test.getValue());
			if (!set.isArray()) set = Json.array().add(set);
			for (Json one : set.asJsonList())
			{
				try
				{
					Json.Schema schema = Json.schema(one.at("schema"));
					for (Json t : one.at("tests").asJsonList())
						tests.add(new SuiteTestJson(test.getKey(),
													t.at("description","***").asString() + "/" +
								  					      one.at("description", "---").asString(),
								  					schema,
								  					t.at("data"),
								  					t.at("valid", true).asBoolean()));
				}
				catch (Throwable t)
				{
					throw new RuntimeException("While adding tests from file " + test.getKey() + " - " + one, t);
				}
			}
		}
		return tests.toArray();
	}
	
	public static void main(String [] argv)
	{
//		String content = TestSchemas.readFile(new File("c:/work/mjson/schema.json"));
//		Json j = Json.read(content);
//		System.out.println(j.toString());
//		Json.schema(Json.object(
//            "$ref","https://raw.githubusercontent.com/json-schema/JSON-Schema-Test-Suite/develop/remotes/subSchemas.json#/refToInteger"
//        ));
		Json set = Json.read(readTextResource("/suite/ref.json"));
		for (Json one : set.asJsonList())
		{
			Json.Schema schema = Json.schema(one.at("schema"));
			//System.out.println(one.at("schema"));
			for (Json t : one.at("tests").asJsonList())
			{
				SuiteTestJson thetest = new SuiteTestJson("properties",
											t.at("description","***").asString() + "/" +
						  					      one.at("description", "---").asString(),
						  					schema,
						  					t.at("data"),
						  					t.at("valid", true).asBoolean());
				try
				{
					thetest.doTest();
				}
				catch (Throwable ex)
				{
					System.out.println("Failed at " + t);
					ex.printStackTrace();
					System.exit(-1);
				}
			}
		}
//		TestSchemas test = new TestSchemas();
//		test.setUp();
//		test.testType();
	}
}
