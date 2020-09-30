import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import io.restassured.response.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.restassured.RestAssured.given;

public class DocDeadLinks {

    public static HashSet<String> getPageLinks(String URL) {
        HashSet<String> links = new HashSet<>();
        if (!links.contains(URL)) {


            Document document = null;
            try {
                document = Jsoup.connect(URL).get();

                Elements linksOnPage = document.select("a[href]");


                for (Element page : linksOnPage) {

                    if(!(page.attr("abs:href").contains("mailto")) && !(page.attr("abs:href").contains("youtube")) && !(page.attr("abs:href").contains("download/attachments")) && !(page.attr("abs:href").contains("github.com")) ){

                        links.add("https://docs-snaplogic.atlassian.net/wiki/spaces/SD/pages/" + URL.split("=")[1] + ">" + page.text() + ">" + page.attr("abs:href"));
                    }
                }
            } catch (IOException e) {
                System.err.println("exception reading page : " + URL + " " + e.getMessage());
            }

        }
        return links;
    }

    public static List<String> getPageIds(String limit, String start) {
        List<String> id = new ArrayList<>();
        String url = "https://docs-snaplogic.atlassian.net/wiki/rest/api/content?spaceKey=SD&limit=" + limit + "&start=" + start;

        Response response = given().redirects().follow(true)
                .get(url);

        ReadContext ctx = JsonPath.parse(response.getBody().asString());
        List<String> entities = ctx.read("$.results.[*].id");
        for (int i = 0; i < entities.size(); i++) {
            id.add(ctx.read("$.results.[" + i + "].id").toString());
        }

        return id;

    }

    public static void main(String[] args) throws Exception {
        List<String> totalLinks = new ArrayList<>();
        List<String> confluencePageLinks = new ArrayList<>();
        List<Map <String,String>> finallist = new ArrayList<>();

        Map<String, String> resultMap = new HashMap<>();


        confluencePageLinks.addAll(getPageIds("1000", "0"));
        confluencePageLinks.addAll(getPageIds("1000", "1000"));

        for (String link : confluencePageLinks) {
            totalLinks.addAll(getPageLinks("https://docs-snaplogic.atlassian.net/wiki/plugins/viewsource/viewpagesrc.action?pageId=" + link));
        }

        for (String lnk : totalLinks) {
            try {
                Response response = given().redirects().follow(true)
                        .get(lnk.split(">")[2]);
                if (response.getStatusCode() != 200 && response.getStatusCode() != 401) {

                    resultMap.put(lnk,  ">" + String.valueOf(response.getStatusCode()));
                }
            } catch (Exception e) {
                System.err.println("exception opening page : " + lnk + " " + e.getMessage());
                resultMap.put(lnk,  ">" + e.getMessage());
            }

        }

        System.out.println(resultMap);
        for (String s:resultMap.keySet()){

            Map<String, String> rowMap = new HashMap<>();
            rowMap.put("Doc Link",s.split(">")[0]);
            rowMap.put("Link Text",s.split(">")[1]);
            rowMap.put("Dead Link",s.split(">")[2]);
            rowMap.put("Response Code",resultMap.get(s));
            finallist.add(rowMap);
        }


        DateFormat df = new SimpleDateFormat("dd-MM-yy");
        Calendar calobj = Calendar.getInstance();

        ExcelUtils.storeDataIntoExcelFile(finallist, "/src/main/resources/DeadLinks.xls","Documentation Dead Links");

    }

}
