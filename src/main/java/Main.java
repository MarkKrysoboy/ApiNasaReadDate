import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.Arrays;

public class Main {

    public static final String PATH_SERVER_URL = "https://api.nasa.gov/planetary/apod?";
    public static ObjectMapper mapper = new ObjectMapper();


    public static void main(String[] args) {
        String api_key = "";
        try (FileReader reader = new FileReader("nasa_api.key")) {
            int c;
            while ((c = reader.read()) != -1) {
                api_key = api_key + (char) c;
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build()) {
            HttpGet request = new HttpGet(PATH_SERVER_URL + api_key);
            CloseableHttpResponse response = httpClient.execute(request);

//            Arrays.stream(response.getAllHeaders()).forEach(System.out::println);
//            String answer = new String(response.getEntity().getContent().readAllBytes());
//            System.out.println(answer);

            NasaAnswer nasaAnswer = mapper.readValue(response.getEntity().getContent().readAllBytes(),
                    new TypeReference<>() {
                    }
            );

            String url = nasaAnswer.getUrl();
            request = new HttpGet(url);
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            response = httpClient.execute(request);

            File file = new File(fileName);
            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                byte[] bytes = response.getEntity().getContent().readAllBytes();
                bos.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
