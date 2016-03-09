package testShit;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.breadcrumbs.database.DBMaster;
import com.breadcrumbs.models.Trail;
import com.breadcrumbs.resource.RetrieveData;

public class TrailTests {
	private Trail trail;
	private RetrieveData retrieve;

	@Before
	public void setUp() throws Exception {
		trail = new Trail();
		retrieve = new RetrieveData();
	}

	@After
	public void tearDown() throws Exception {
		//retrieve.Obliterate(); // Get rid of everything
	}
	
	@Test
	public void TestThatICanUpdateTrailDistance() {
		// Create user And trail
		String id = retrieve.CreateNewUser("Josiah", "7873", "23", "M", "1", "", "0");
		String trailId = retrieve.SaveTrail("OOOHRAA", "just testing yo", id);
		trail.updateDistance(trailId, "-37", "147", "-24", "147");
		DBMaster dbm = DBMaster.GetAnInstanceOfDBMaster();
		String dist1 = dbm.GetStringPropertyFromNode(trailId, "Distance");
		int dist2 = Integer.getInteger(dist1);
		assertTrue(dist2 > 0);
	}
	
	@Test
	public void TestThatStartDateIsSetCorrectly() {
		String id = retrieve.CreateNewUser("Josiah", "7873", "23", "M", "1", "","0");
		String trailId = retrieve.SaveTrail("OOOHRAA", "just testing yo", id);
		DBMaster dbm = DBMaster.GetAnInstanceOfDBMaster();
		String date = dbm.GetStringPropertyFromNode(trailId, "StartDate");
		assertTrue(date.equals("03/02/2016"));
	}
}
