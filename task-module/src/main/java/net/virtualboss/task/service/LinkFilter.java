package net.virtualboss.task.service;

import net.virtualboss.common.model.entity.TaskAttachment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LinkFilter {

    private LinkFilter() {
        throw new IllegalStateException("Utility class");
    }

    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(?i)\\b(https?://|ftp://|file://)([^\\s;\\]\\[<>]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IMAGE_PATTERN = Pattern.compile(
            "(?i)\\.(jpg|png|gif)$"
    );

    public static String filterLinks(Set<TaskAttachment> attachments, boolean isFilesField, boolean jsonFormat) {

        if (attachments == null) return "";

        String text = attachments.stream().map(attachment -> {
                    String allFullPath = attachment.getResource().getAllFullPath();
                    String uncPath = attachment.getResource().getUncFullPath();
                    return allFullPath + " " + uncPath;
                }
        ).collect(Collectors.joining("\r"));

        if (text.isBlank()) return "";

        List<String> links = extractLinks(text);
        if (links.isEmpty()) return "";

        links = processDuplicates(links, isFilesField);
        int maxLength = calculateMaxLength(links);

        StringBuilder result = new StringBuilder();
        for (String link : links) {
            String displayText = shortenLink(link, maxLength);
            String title = extractFileName(link);
            boolean isImage = isImage(link);

            appendLink(result, link, displayText, title, isImage, jsonFormat);
        }

        return formatResult(result, jsonFormat);
    }

    private static List<String> extractLinks(String text) {
        List<String> links = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(text);

        while (matcher.find()) {
            String protocol = matcher.group(1).toLowerCase();
            String path = matcher.group(2);
            links.add(protocol + path.replaceAll("[\\r\\n]", ""));
        }

        return links;
    }

    private static List<String> processDuplicates(List<String> links, boolean isFilesField) {
        if (!isFilesField) return links;

        Set<String> uniqueLinks = new LinkedHashSet<>();
        for (int i = 1; i < links.size(); i += 2) {
            uniqueLinks.add(links.get(i));
        }
        return new ArrayList<>(uniqueLinks);
    }

    private static int calculateMaxLength(List<String> links) {
        int max = 40;
        int min = 26;

        for (String link : links) {
            if (!link.startsWith("file://")) {
                min = Math.max(min, link.length());
            }
        }
        return Math.min(max, min);
    }

    private static String shortenLink(String link, int maxLength) {
        String[] protocols = {"https://", "http://", "ftp://", "file://"};
        for (String proto : protocols) {
            if (link.regionMatches(true, 0, proto, 0, proto.length())) {
                String path = link.substring(proto.length());
                return processPath(path, maxLength);
            }
        }
        return link;
    }

    private static String processPath(String path, int maxLength) {
        String[] parts = path.split("/");
        StringBuilder shortPath = new StringBuilder();

        for (String part : parts) {
            if (shortPath.length() + part.length() + 1 > maxLength) {
                shortPath.append("...");
                break;
            }
            if (!shortPath.isEmpty()) shortPath.append("/");
            shortPath.append(part);
        }
        return shortPath.toString();
    }

    private static String extractFileName(String link) {
        int lastSlash = link.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < link.length() - 1) {
            return link.substring(lastSlash + 1);
        }
        return link;
    }

    private static boolean isImage(String link) {
        return IMAGE_PATTERN.matcher(link).find();
    }

    private static void appendLink(StringBuilder result, String link,
                                   String displayText, String title,
                                   boolean isImage, boolean jsonFormat) {
        String escapedLink = link.replace("\"", "%22")
                .replace(" ", "%20")
                .replace("&", "%26")
                .replace("#", "%23");

        String cleanDisplay = displayText.replace("%20", " ")
                .replace("%26", "&")
                .replace("%23", "#");

        if (jsonFormat) {
            result.append(String.format("\"%s\"", cleanDisplay));
            return;
        }

        if (isImage) {
            result.append(String.format(
                    "<a href=\"%s\" target=\"_blank\" title=\"%s\">" +
                    "<img src=\"%s\"></a><br>",
                    escapedLink, title, escapedLink
            ));
        } else {
            result.append(String.format(
                    "<a href=\"%s\" target=\"_blank\" title=\"%s\">%s</a><br>",
                    escapedLink, title, cleanDisplay
            ));
        }
    }

    private static String formatResult(StringBuilder result, boolean jsonFormat) {
        if (jsonFormat) {
            String links = result.toString();
            String formatted = links.replace("\"\"", "\",\"");
            return "[" + formatted + "]";
        }
        return "<font size=2 face=\"arial\">" + result.toString() + "</font>";
    }
}
