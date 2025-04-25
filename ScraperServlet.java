import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ScraperServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        HttpSession session = req.getSession();
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) visitCount = 0;
        visitCount++;
        session.setAttribute("visitCount", visitCount);

        String url = req.getParameter("url");
        boolean getTitle = req.getParameter("title") != null;
        boolean getLinks = req.getParameter("links") != null;
        boolean getImages = req.getParameter("images") != null;

        ScrapeResult result = new ScrapeResult();

        Document doc = Jsoup.connect(url).get();
        if (getTitle) {
            result.title = doc.title();
        }

        if (getLinks) {
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                result.links.add(link.attr("abs:href"));
            }
        }

        if (getImages) {
            Elements images = doc.select("img[src]");
            for (Element img : images) {
                result.images.add(img.attr("abs:src"));
            }
        }

        Gson gson = new Gson();
        String json = gson.toJson(result);
        PrintWriter out = res.getWriter();
        out.println("<html>");
        out.println("<head>");

        out.println("<script>");
        out.println("function downloadCSV() {");
        out.println("    let csv = 'Type,Data\\n';");
        if (getTitle) {
            out.println("    csv += 'Title,\"' + " + escapeJS(result.title) + " + '\"\\n';");
        }
        if (getLinks) {
            for (String link : result.links) {
                out.println("    csv += 'Link,\"' + " + escapeJS(link) + " + '\"\\n';");
            }
        }
        if (getImages) {
            for (String img : result.images) {
                out.println("    csv += 'Image,\"' + " + escapeJS(img) + " + '\"\\n';");
            }
        }
        out.println("    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });");
        out.println("    const url = URL.createObjectURL(blob);");
        out.println("    const link = document.createElement('a');");
        out.println("    link.setAttribute('href', url);");
        out.println("    link.setAttribute('download', 'scrape_results.csv');");
        out.println("    document.body.appendChild(link);");
        out.println("    link.click();");
        out.println("    document.body.removeChild(link);");
        out.println("}");
        out.println("</script>");

        out.println("</head><body>");
        out.println("<h2>Scraped Results</h2>");

        out.println("<p>You have visited this page <strong>" + visitCount + "</strong> times.</p>");

        out.println("<table border='1'>");
        out.println("<tr><th>Type</th><th>Data</th></tr>");

        if (getTitle) {
            out.println("<tr><td>Title</td><td>" + result.title + "</td></tr>");
        }

        if (getLinks) {
            for (String link : result.links) {
                out.println("<tr><td>Link</td><td>" + link + "</td></tr>");
            }
        }

        if (getImages) {
            for (String img : result.images) {
                out.println("<tr><td>Image</td><td>" + img + "</td></tr>");
            }
        }

        out.println("</table><br>");

        out.println("<button onclick='downloadCSV()'>Download Results as CSV</button>");

        out.println("<h3>JSON Output:</h3>");
        out.println("<pre>" + json + "</pre>");

        out.println("</body></html>");
    }

    private String escapeJS(String input) {
        if (input == null) return "''";
        return "'" + input.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\"") + "'";
    }

    static class ScrapeResult {
        String title;
        List<String> links = new ArrayList<>();
        List<String> images = new ArrayList<>();
    }
}