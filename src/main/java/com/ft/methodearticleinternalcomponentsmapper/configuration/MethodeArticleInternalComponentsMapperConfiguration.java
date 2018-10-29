package com.ft.methodearticleinternalcomponentsmapper.configuration;

import com.ft.bodyprocessing.richcontent.VideoSiteConfiguration;
import com.ft.platform.dropwizard.AppInfo;
import com.ft.platform.dropwizard.ConfigWithAppInfo;
import com.ft.platform.dropwizard.ConfigWithGTG;
import com.ft.platform.dropwizard.GTGConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;

public class MethodeArticleInternalComponentsMapperConfiguration extends Configuration implements ConfigWithAppInfo, ConfigWithGTG {

    private final ConsumerConfiguration consumerConfiguration;
    private final ProducerConfiguration producerConfiguration;
    private final UppServiceConfiguration documentStoreApiConfiguration;
    private final UppServiceConfiguration concordanceApiConfiguration;
    private final UppServiceConfiguration methodeArticleMapperConfiguration;
    private final UppServiceConfiguration methodeContentPlaceholderMapperConfiguration;
    private final ValidationConfiguration validationConfiguration;
    private final List<VideoSiteConfiguration> videoSiteConfig;
    private final List<String> interactiveGraphicsWhiteList;
    private final String contentUriPrefix;
    private final String canonicalUrlTemplate;
    @JsonProperty
    private final GTGConfig gtgConfig = new GTGConfig();
    @JsonProperty
    private AppInfo appInfo = new AppInfo();

    public MethodeArticleInternalComponentsMapperConfiguration(@JsonProperty("consumer") ConsumerConfiguration consumerConfiguration,
                                                               @JsonProperty("producer") ProducerConfiguration producerConfiguration,
                                                               @JsonProperty("documentStoreApi") UppServiceConfiguration documentStoreApiConfiguration,
                                                               @JsonProperty("concordanceApi") UppServiceConfiguration concordanceApiConfiguration,
                                                               @JsonProperty("videoSiteConfig") List<VideoSiteConfiguration> videoSiteConfig,
                                                               @JsonProperty("interactiveGraphicsWhiteList") List<String> interactiveGraphicsWhiteList,
                                                               @JsonProperty("methodeArticleMapper") UppServiceConfiguration methodeArticleMapperConfiguration,
                                                               @JsonProperty("methodeContentPlaceholderMapper") UppServiceConfiguration methodeContentPlaceholderMapperConfiguration,
                                                               @JsonProperty("validationConfiguration") ValidationConfiguration validationConfiguration,
                                                               @JsonProperty("contentUriPrefix") String contentUriPrefix,
                                                               @JsonProperty("canonicalUrlTemplate") String canonicalUrlTemplate) {
        this.consumerConfiguration = consumerConfiguration;
        this.producerConfiguration = producerConfiguration;
        this.documentStoreApiConfiguration = documentStoreApiConfiguration;
        this.concordanceApiConfiguration = concordanceApiConfiguration;
        this.videoSiteConfig = videoSiteConfig;
        this.interactiveGraphicsWhiteList = interactiveGraphicsWhiteList;
        this.methodeArticleMapperConfiguration = methodeArticleMapperConfiguration;
        this.methodeContentPlaceholderMapperConfiguration = methodeContentPlaceholderMapperConfiguration;
        this.validationConfiguration = validationConfiguration;
        this.contentUriPrefix = contentUriPrefix;
        this.canonicalUrlTemplate = canonicalUrlTemplate;
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
    public UppServiceConfiguration getConcordanceApiConfiguration() {
        return concordanceApiConfiguration;
    }

    @NotNull
    public UppServiceConfiguration getDocumentStoreApiConfiguration() {
        return documentStoreApiConfiguration;
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
    public String getContentUriPrefix() {
        return contentUriPrefix;
    }

    @NotNull
    public UppServiceConfiguration getMethodeArticleMapperConfiguration() {
        return methodeArticleMapperConfiguration;
    }

    @NotNull
    public UppServiceConfiguration getMethodeContentPlaceholderMapperConfiguration() {
        return methodeContentPlaceholderMapperConfiguration;
    }

    @NotNull
    public ValidationConfiguration getValidationConfiguration() {
        return validationConfiguration;
    }

    public String getCanonicalUrlTemplate() {
        return canonicalUrlTemplate;
    }

    @Override
    public AppInfo getAppInfo() {
        return appInfo;
    }

    @Override
    public GTGConfig getGtg() {
        return gtgConfig;
    }
}
