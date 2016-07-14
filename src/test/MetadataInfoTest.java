package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.xmpbox.xml.XmpParsingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pmedit.FieldID;
import pmedit.MetadataInfo;

public class MetadataInfoTest {
	
	public static class PMTuple{
		final File file;
		final MetadataInfo md;
		public PMTuple(File file, MetadataInfo md){
			this.file = file;
			this.md = md;
		}
	}
	
	public static File emptyPdf() throws Exception{
		File temp = File.createTempFile("test-file", ".pdf");
        PDDocument doc = new PDDocument();
        try {
            // a valid PDF document requires at least one page
            PDPage blankPage = new PDPage();
            doc.addPage(blankPage);
            doc.save(temp);
        } finally {
            doc.close();
        }
        temp.deleteOnExit();
        return temp;
	}
	
	public static List<PMTuple> randomFiles(int numFiles) throws Exception{
		List<String> fields = MetadataInfo.keys();
		int numFields = fields.size();
		List<PMTuple> rval = new ArrayList<MetadataInfoTest.PMTuple>();
		
		Random rand = new Random();
		for(int i=0; i<numFiles; ++i) {
			MetadataInfo md = new MetadataInfo();
			int genFields = rand.nextInt(numFields);
			for(int j=0; j< genFields; ++j){
				String field = fields.get(rand.nextInt(numFields));
				if(field.equals("doc.trapped")){
					md.setAppend(field, Arrays.asList("False", "True","Unknown").get(rand.nextInt(3)));
					continue;
				}
				switch(MetadataInfo.getFieldDescription(field).type){
				case IntField:
					md.setAppend(field, rand.nextInt(1000));
					break;
				case DateField:
					Calendar cal = Calendar.getInstance();
					//cal.set(Calendar.MILLISECOND, 0);
					cal.setLenient(false);
					md.setAppend(field, cal);
					break;
				default:
					md.setAppend(field, new BigInteger(130, rand).toString(32));
					break;
				}
			}
			File pdf = emptyPdf();
			md.saveToPDF(pdf);
			rval.add(new PMTuple(pdf, md));
		}
		return rval;
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSimpleEquality() {
		assertTrue(new MetadataInfo().isEquvalent(new MetadataInfo()));
		assertTrue(MetadataInfo.getSampleMetadata().isEquvalent(MetadataInfo.getSampleMetadata()));
		
		MetadataInfo md1 = new MetadataInfo();
		MetadataInfo md2 = new MetadataInfo();
		md1.setAppendFromString("doc.title", "a title");
		assertFalse(md1.isEquvalent(md2));
		
		md2.setAppendFromString("doc.title", md1.getString("doc.title"));
		assertTrue(md1.isEquvalent(md2));

		md1.setAppendFromString("basic.rating", "333");
		assertFalse(md1.isEquvalent(md2));
		
		md2.setAppendFromString("basic.rating", "333");
		assertTrue(md1.isEquvalent(md2));
	}
	
	@Test
	public void testEmptyLoad() throws Exception, IOException, XmpParsingException, Exception{
		MetadataInfo md = new MetadataInfo();
		md.loadFromPDF(emptyPdf());
		assertTrue(md.isEquvalent(new MetadataInfo()));
	}
	
	@Test
	public void testFuzzing() throws Exception {
		for(PMTuple t: randomFiles(100)){
			MetadataInfo loaded = new MetadataInfo();
			loaded.loadFromPDF(t.file);
			//System.out.println(pdf.getAbsolutePath());
			assertTrue(t.md.isEquvalent(loaded));
		}
	}

}