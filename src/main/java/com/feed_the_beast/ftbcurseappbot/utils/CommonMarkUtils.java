package com.feed_the_beast.ftbcurseappbot.utils;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Arrays;
import java.util.List;

public class CommonMarkUtils {
    private Parser parser;
    private HtmlRenderer renderer;

    public CommonMarkUtils () {
        List<Extension> extensions = Arrays.asList(TablesExtension.create(), StrikethroughExtension.create());
        parser = Parser.builder().extensions(extensions).build();
        renderer = HtmlRenderer.builder().extensions(extensions).build();

    }

    public String renderToHTML (String md) {
        Node document = parser.parse(md);
        return renderer.render(document);

    }

    public static String Bold (String in) {
        return "**" + in + "**";
    }

    public static String Italics (String in) {
        return "*" + in + "*";
    }

    public static String h1 (String in) {
        return "# " + in;
    }

    public static String h2 (String in) {
        return "## " + in;
    }

    public static String h3 (String in) {
        return "### " + in;
    }

    public static String link (String title, String link) {
        return "[" + title + "](" + link + ")";
    }

    public static String image (String image) {
        return "!" + link("Image", image);
    }

    public static String blockquote (String in) {
        return "> " + in;
    }

    public static String list (String in) {
        return "* " + in;
    }

    public static String horizontalRule () {
        return "---";
    }

    public static String inline (String in) {
        return "`" + in + "`";
    }

    public static String codeBlock (String in) {
        return "```\n" + in + "\n```";
    }

}
