import by.gto.xchanger.Importer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class XChangerServiceRootTest {

    @Before
    public void setUp() throws Exception {
//        TestPreparer.prepare();
//        ac = ApplicationContextProvider.getApplicationContext();
//        eripService = (EripService) ac.getBean("eripService");
    }

    @Test
    public void testConstructPhotoName() {
        String[] strings = Importer.constructPhotoName("511a1a69-d0a2-472d-a855-204b9b9b86a2.jpg");
        Assert.assertEquals("511a1a69/d0a2472d/a855204b/9b9b86a2", strings[0]);
        Assert.assertEquals("511a1a69/d0a2472d/a855204b", strings[1]);

        strings = Importer.constructPhotoName("511a1a69-d0a2-472d-a855-204b9b9b86a2.png");
        Assert.assertEquals("511a1a69/d0a2472d/a855204b/9b9b86a2", strings[0]);
        Assert.assertEquals("511a1a69/d0a2472d/a855204b", strings[1]);

        strings = Importer.constructPhotoName("511a1a69-d0a2-472d-a855-204b9b9b86a2");
        Assert.assertEquals("511a1a69/d0a2472d/a855204b/9b9b86a2", strings[0]);
        Assert.assertEquals("511a1a69/d0a2472d/a855204b", strings[1]);

        Assert.assertNull(Importer.constructPhotoName(null));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testConstructPhotoName2() {
        Importer.constructPhotoName("511a1a69-d0a2-472d-a855-204b9b9b86");
    }

}
