package search;

import java.io.IOException; 
import org.apache.lucene.document.Document; 
import org.apache.lucene.queryParser.ParseException; 
import org.apache.lucene.search.ScoreDoc; 
import org.apache.lucene.search.TopDocs; 
public class LuceneTester 
{ 
	String indexDir = "dat\\index"; 
	String dataDir = "dat\\data"; 
	Indexer indexer; 
	Searcher searcher;
        public static String result="";
	public static String main(String query) 
	{ 
                result="";
		LuceneTester tester; 
		try 
		{ 
			tester = new LuceneTester(); 
			tester.createIndex(); 
			tester.search(query); 
		} 
		catch (IOException e) 
		{ 
			e.printStackTrace(); 
		} 
		catch (ParseException e) 
		{ 
			e.printStackTrace(); 
		}
                
                return result;
	} 
	private void createIndex() throws IOException
	{ 
		indexer = new Indexer(indexDir); 
		int numIndexed; 
		long startTime = System.currentTimeMillis(); 
		numIndexed = indexer.createIndex(dataDir, new TextFileFilter()); 
		long endTime = System.currentTimeMillis(); 
		indexer.close(); 
		System.out.println(numIndexed+" File indexed, time taken: " +(endTime-startTime)+" ms"); 
	} 
	private void search(String searchQuery) throws IOException, ParseException
	{ 
		searcher = new Searcher(indexDir); 
		long startTime = System.currentTimeMillis(); 
		TopDocs hits = searcher.search(searchQuery); 
		long endTime = System.currentTimeMillis(); 
		System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime)); 
		for(ScoreDoc scoreDoc : hits.scoreDocs) 
		{ 
                    Document doc = searcher.getDocument(scoreDoc); 
                    //System.out.println("File: " + doc.get(LuceneConstants.FILE_PATH)); 
                    result+="File: " + doc.get(LuceneConstants.FILE_PATH)+"\n";
                } 
		searcher.close(); 
	} 
}