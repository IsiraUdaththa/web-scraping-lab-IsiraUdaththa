import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {
    public static void main(String[] args) throws IOException {
        
        final String url = "https://bbc.com";

        try {
            Document doc = Jsoup.connect(url).get();
            System.out.println("Title: " + doc.title());

            for (int i = 1; i <= 6; i++) {
                Elements headings = doc.select("h" + i);
                for (Element heading : headings) {
                    System.out.println("H" + i + ": " + heading.text());
                }
            }

            Elements links = doc.select("a[href]");
            System.out.println("\nLinks:");
            for (Element link : links) {
                System.out.println(link.attr("abs:href") + " - " + link.text());
            }


            // Let's use JSON response that's embedded in a <script> tag (__NEXT_DATA__) within the webpage.
            // Because Its much more convenient
            List<NewsArticle> articles = new ArrayList<>();

            Element scriptTag = doc.getElementById("__NEXT_DATA__");
            String jsonText = scriptTag.html();

            JSONObject root = new JSONObject(jsonText);
            JSONObject page = root.getJSONObject("props").getJSONObject("pageProps").getJSONObject("page").getJSONObject("@\"home\",");

            JSONArray sections = page.getJSONArray("sections");
            for (int i = 0; i < sections.length(); i++) {
                JSONObject section = sections.getJSONObject(i);
                try {
                    JSONArray content = section.getJSONArray("content");
                    System.out.println(content);
                    for (int j = 0; j < content.length(); j++) {
                        JSONObject article = content.getJSONObject(j);
                        NewsArticle newsArticle = new NewsArticle();
                        newsArticle.setHeadline(article.optString("headline"));
                        newsArticle.setDescription(article.optString("description"));
                        newsArticle.setImage(article.optString("image"));
                        newsArticle.setLink(article.optString("link"));
                        articles.add(newsArticle);
                    }
                } catch (RuntimeException e) {
                    continue;
                }
            }

            // TODO: to find the published date and authors we have to go through each article. 


        } catch (IOException e) {
            System.out.println("Error fetching the webpage: " + e.getMessage());
        }
    }

    static class NewsArticle {
        String headline;
        String publicationDate;
        String author;
        String description;
        String image;
        String link;

        public NewsArticle() {
        }

        @Override
        public String toString() {
            return "Headline: " + headline + "\nDate: " + publicationDate + "\nAuthor: " + author + "\n";
        }

        public String getHeadline() {
            return headline;
        }

        public void setHeadline(String headline) {
            this.headline = headline;
        }

        public String getPublicationDate() {
            return publicationDate;
        }

        public void setPublicationDate(String publicationDate) {
            this.publicationDate = publicationDate;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
}
