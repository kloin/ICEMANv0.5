package testShit;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.breadcrumbs.resource.RetrieveData;
import com.breadcrumbs.search.Search;

public class SearchTests {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test 
	public void testThatSearchRetrievesData() {
		Search searchClass = new Search();
		String test = searchClass.SearchAllNodesWithLabelByGivenProperty("josi");
		System.out.println(test);
	}
}
