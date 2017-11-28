package de.mpg.pks.bbecker.webcrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * Created by bbecker on 28.11.17.
 *
 * technicserver - run modpacks from technicpack.net as server with ease.
 * Copyright (C) 2017 Bennet Becker <bbecker@pks.mpg.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Parser {
    private HashSet<String> links_attended;
    private String host;

    public static TreeNode parse(String host){
        Parser parser = new Parser(host);
        return parser._parse("http://" + host + "/wiki/Main_Page");
    }

    private Parser(String host){
        this.host = host;
        links_attended = new HashSet<>();
    }

    private TreeNode _parse(String link) {
        try {
            System.out.println("parsing " + link);
            Document doc = Jsoup.connect(link).get();
            Elements links = doc.select("body div#content a");

            TreeNode tree = new TreeNode(doc.title(), doc.location(), doc.select("body div#content").outerHtml(), doc.outerHtml());

            Stream<String> internal_links = links.stream().filter(
                    l -> !l.hasClass("extern")).map(l -> l.attr("href"))
                    .filter(
                            l -> l.startsWith("/wiki/")
                            && !link.endsWith(l)
                            && links_attended.stream().noneMatch(a -> a.endsWith(l))
                            && !l.contains("#")
                            && !l.contains("Special:")
                            && !l.contains("File:")
                            && !l.contains("Category:"));

            links_attended.add(link);

            internal_links.map(l -> _parse("http://" + host + l)).forEach(tree::addNext);

            return tree;
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
