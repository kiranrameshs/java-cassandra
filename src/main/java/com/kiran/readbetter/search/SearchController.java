package com.kiran.readbetter.search;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SearchController {

    private WebClient webClient;
    private final String prefixURL = "https://covers.openlibrary.org/b/id/";

    public SearchController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024)).build())
                .baseUrl("https://openlibrary.org/search.json").build();

    }

    @GetMapping(value = "/search")
    public String getSearchResult(@RequestParam String query, Model model){
        Mono<SearchResult> searchUrl = this.webClient.get().uri("?q={query}", query)
                .retrieve().bodyToMono(SearchResult.class);
        SearchResult results = searchUrl.block();
        List<SearchResultBook> searchResults = results.getDocs().stream()
                .limit(10)
                .map(result ->
                {
                    result.setKey(result.getKey().replace("/works/", ""));
                    String coverId = result.getCover_i();
                    String coveImgUrl;
                    if(StringUtils.hasText(coverId)){
                        coveImgUrl = prefixURL + result.getCover_i() + "-M.jpg";
                    }else{
                        coveImgUrl = "/images/no-image.png";
                    }
                     result.setCover_i(coveImgUrl);
                    return result;
                })
                .collect(Collectors.toList());
        model.addAttribute("searchResults", searchResults);

        return "search";

    }
}
