package com.wolfe.catalog;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class SeoController {
    private final ProductRepository products;
    private final String baseUrl;
    public SeoController(ProductRepository products, @Value("${WOLFE_PUBLIC_BASE_URL:http://localhost}") String baseUrl) { this.products=products; this.baseUrl=baseUrl.replaceAll("/$",""); }
    @GetMapping(value="/sitemap.xml", produces=MediaType.APPLICATION_XML_VALUE)
    public String sitemap() {
        StringBuilder x=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
        for(String path:new String[]{"/","/shop","/bundles","/catalogue","/consultation","/quote","/custom-design","/journal","/privacy-policy","/terms"}) x.append("<url><loc>").append(baseUrl).append(path).append("</loc></url>");
        products.findAllByActiveTrueOrderByIdAsc(org.springframework.data.domain.PageRequest.of(0, 50000, org.springframework.data.domain.Sort.by("id"))).forEach(p -> x.append("<url><loc>").append(baseUrl).append("/product/").append(URLEncoder.encode(p.getSlug(),StandardCharsets.UTF_8)).append("</loc></url>"));
        return x.append("</urlset>").toString();
    }
    @GetMapping(value="/robots.txt", produces=MediaType.TEXT_PLAIN_VALUE)
    public String robots(){ return "User-agent: *\nAllow: /\nDisallow: /admin\nDisallow: /account\nDisallow: /checkout\nDisallow: /orders\nDisallow: /profile\nDisallow: /addresses\nDisallow: /notifications\nDisallow: /wishlist\nDisallow: /compare\nSitemap: "+baseUrl+"/sitemap.xml\n"; }
}
