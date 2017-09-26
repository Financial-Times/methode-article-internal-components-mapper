package com.ft.methodearticleinternalcomponentsmapper.transformation;

import com.ft.methodearticleinternalcomponentsmapper.transformation.InteractiveGraphicsMatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InteractiveGraphicsMatcherTest {

    private static final List<String> WHITELIST = Arrays.asList(
            "http://interactive.ftdata.co.uk/features/2013-12-18_tagCloud/",
            "http://www.ft.com/ig/features/2013-11-22_seasonal_appeal_storypack/dist/index.html",
            "http://www.ft.com/ig/features/2014-01-14_Davos/index.html",
            "http://www.ft.com/ig/widgets/sortable-table/v1/widget/index.html?key=0AuFKVrLBg8OSdG1PRE5pRDY4djluazZrU2lrVmtVSnc",
            "http://www.ft.com/ig/widgets/non-farm-payrolls/1.0.0/dist/index.html?key=April_2014",
            "http://www.ft.com/ig/features/alibaba-ipo-comparison/",
            "http://ft.cartodb.com/viz/5a6fe70e-b072-11e4-8584-0e4fddd5de28/embed_map",
            "http://ig.ft.com/features/2014-02-17_clingons/index.html"
    );
    private static final List<String> BLACKLIST = Arrays.asList(
            "http://www.youtube.com/embed/MOaFYQ4fL9g?list=UUoUxsWakJucWg46KW5RsvPw",
            "http://rcm-eu.amazon-adsystem.com/e/cm?t=finantimes-21&amp;o=2&amp;p=8&amp;l=as1&amp;asins=B00JAD6GKG&amp;ref=tf_til&amp;fc1=000000&amp;IS2=1&amp;lt1=_blank&amp;m=amazon&amp;lc1=0000FF&amp;bc1=000000&amp;bg1=FFFFFF&amp;f=ifr",
            "http://markets.ft.com/RESEARCH/API/ChartBuilder?t=equities&amp;p=eyJzeW1ib2wiOiIxODMzMjgiLCJyZWdpb24iOm51bGwsImhlaWdodCI6IjE5MyIsIndpZHRoIjoiMjcyIiwibGluZVN0eWxlIjoibGluZSIsImR1cmF0aW9uIjoiMzY1Iiwic3RhcnREYXRlIjpudWxsLCJlbmREYXRlIjpudWxsLCJwcmltYXJ5TGFiZWwiOiJMdWZ0aGFuc2Egc2hhcmUgcHJpY2UiLCJzZWNvbmRhcnlMYWJlbCI6IjEgeWVhciB0byBKdWwgOSIsInRlcnRpYXJ5TGFiZWwiOm51bGwsInF1YXRlcm5hcnlMYWJlbCI6bnVsbCwiaXNNb2JpbGUiOmZhbHNlLCJTaG93RGlzY2xhaW1lciI6dHJ1ZSwidW5pdCI6InB4In0=",
            "http://ig.ft.com/widgets/twitterWidget/?tweet/527225682414567424",
            "http://financialtimes.polldaddy.com/s/age-attitude",
            "http://www.reuters.com/assets/iframe/yovideo?videoId=363520653",
            "http://www.ft.com/ig/widgets/widgetBrowser/audio/podcast.html?#i0t1u2n3e4s5=http://podcast.ft.com/index.php?sid=29&amp;&amp;&amp;&amp;&amp;i0m1a2g3e4=http://podcast.ft.com/media/images/104_new_news.jpg&amp;&amp;&amp;&amp;&amp;t0i1t2l3e4=Apple%20plans%20TV%20streaming%20service&amp;&amp;&amp;&amp;&amp;d0a1t2e3=Mar%2018,%202015%20-%202:52%20am&amp;&amp;&amp;&amp;&amp;b0o1d2y3=Apple%20is%20renewing%20its%20assault%20on%20the%20living%20room.%20%20The%20company%20is%20in%20advanced%20talks%20with%20US%20broadcasters%20to%20launch%20a%20subscription%20streaming%20offering%20with%20plans%20to%20create%20an%20online%20TV%20streaming%20service%20later%20this%20year.%20Ravi%20Mattu%20discusses%20the%20development%20with%20Tim%20Bradshaw.&amp;&amp;&amp;&amp;&amp;a0u1d2i3o4=http://podcast.ft.com/download/29/2588/apletv.mp3",
            "https://player.vimeo.com/video/120274054",
            "https://embed.spotify.com/?uri=spotify:user:ftinteractive:playlist:0zaZHGZ6Y5ordPYZGKFb7U",
            "http://pilot.touchcast.com/e/303",
            "http://interactive.ftdata.co.uk/_other/ben/twitter1.1/get.html?tweet/411659536286883840",
            "http://interactive.ftdata.co.uk/widgets/audioSidebarTiny/index.html#title=Audiobook,audioURL=http://interactive.ftdata.co.uk/audio/boardroombattles.mp3,about=Listen%20to%20Stephen%20Foley%20read%20this%20story",
            "http://interactive.ftdata.co.uk/widgets/audioSidebarTiny2/index.html#$title=Podcast,$audioURL=http://ftmedia.podhoster.com/ft/branson.mp3,$about=Listen",
            "http://ft.cartodb.com.malicous.site.com/viz/5a6fe70e-b072-11e4-8584-0e4fddd5de28/embed_map"
    );

    private static final List<String> RULES = Arrays.asList(
            "http://interactive.ftdata.co.uk/(?!(_other/ben/twitter)|(widgets/audio)).*",
            "http://(www.)?ft.com/ig/(?!widgets/widgetBrowser/audio).*",
            "http://ig.ft.com/features.*",
            "http://ft.cartodb.com/.*"
    );
    @Test
    public void shouldTransformWhitelist() throws Exception {
        final InteractiveGraphicsMatcher matcher = new InteractiveGraphicsMatcher(RULES);
        for (final String url :WHITELIST) {
            assertTrue(String.format("Url [%s] is not whitelisted", url), matcher.matches(url));
        }
    }

    @Test
    public void shouldNotTransformBlacklist() throws Exception {
        final InteractiveGraphicsMatcher matcher = new InteractiveGraphicsMatcher(RULES);
        for (final String url :BLACKLIST) {
            assertFalse(String.format("Url [%s] is not blacklisted", url),matcher.matches(url));
        }
    }
}
