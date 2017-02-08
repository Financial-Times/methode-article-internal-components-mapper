package com.ft.methodearticleinternalcomponentsmapper.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.bodyprocessing.richcontent.VideoSiteConfiguration;
import com.ft.content.model.Brand;

import io.dropwizard.Configuration;

import java.util.List;

public class MethodeArticleInternalComponentsMapperConfiguration extends Configuration {

    private final MethodeArticleMapperConfiguration methodeArticleMapperConfiguration;
    private final ConsumerConfiguration consumerConfiguration;
    private final ProducerConfiguration producerConfiguration;
    private final Brand financialTimesBrand;
    private final List<VideoSiteConfiguration> videoSiteConfig;
    private final List<String> interactiveGraphicsWhiteList;
    private final String contentUriPrefix;

    public MethodeArticleInternalComponentsMapperConfiguration(@JsonProperty("consumer") ConsumerConfiguration consumerConfiguration,
                                                               @JsonProperty("producer") ProducerConfiguration producerConfiguration,
                                                               @JsonProperty("financialTimesBrandId") String financialTimesBrandId,
                                                               @JsonProperty("methodeArticleMapper") MethodeArticleMapperConfiguration methodeArticleMapperConfiguration, @JsonProperty("videoSiteConfig") List<VideoSiteConfiguration> videoSiteConfig,
                                                               @JsonProperty("interactiveGraphicsWhiteList") List<String> interactiveGraphicsWhiteList,
                                                               @JsonProperty("contentUriPrefix") String contentUriPrefix) {

        this.financialTimesBrand = new Brand(financialTimesBrandId);
        this.consumerConfiguration = consumerConfiguration;
        this.producerConfiguration = producerConfiguration;
        this.methodeArticleMapperConfiguration = methodeArticleMapperConfiguration;
        this.videoSiteConfig = videoSiteConfig;
        this.interactiveGraphicsWhiteList = interactiveGraphicsWhiteList;
        this.contentUriPrefix = contentUriPrefix;
    }

    @NotNull
    public Brand getFinancialTimesBrand() {
        return financialTimesBrand;
    }

    @NotNull
    public List<VideoSiteConfiguration> getVideoSiteConfig() {
        return videoSiteConfig;
    }

    @NotNull
    public List<String> getInteractiveGraphicsWhitelist() {
        return interactiveGraphicsWhiteList;
    }

    @NotNull
    public ConsumerConfiguration getConsumerConfiguration() {
        return consumerConfiguration;
    }

    @NotNull
    public ProducerConfiguration getProducerConfiguration() {
        return producerConfiguration;
    }

    @NotNull
    public String getContentUriPrefix() {
        return contentUriPrefix;
    }

    @NotNull
    public MethodeArticleMapperConfiguration getMethodeArticleMapperConfiguration() {
        return methodeArticleMapperConfiguration;
    }
}
