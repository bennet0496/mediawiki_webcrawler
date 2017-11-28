package de.mpg.pks.bbecker.webcrawler;

import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
public class TreeNode {
    private String name;
    private String link;
    private String content;
    private String html;

    private List<TreeNode> next;

    public TreeNode(String name, String link, String content, String html) {
        this.name = name;
        this.link = link;
        this.content = content;
        this.html = html;
        next = new LinkedList<TreeNode>();
    }

    public boolean treeHasLink(String link) {
        return this.link.equals(link) || next.size() != 0 && next.stream().anyMatch(t -> t.treeHasLink(link));
    }

    public void writeTo(String path, ExportType exportType) throws IOException {
        System.out.println("Exporting " + link + " as " + exportType + " to " + path);

        String suffix = Iterables.getLast(Arrays.asList(link.split("/")));
        File file = new File(path + "/" + suffix + ".html");
        switch (exportType){
            case XML:
                file = new File(path + "/" + suffix + ".xml");
                URLConnection conn = new URL(link.replace("/wiki/", "/wiki/Special:Export/")).openConnection();
                FileUtils.copyInputStreamToFile(conn.getInputStream(), file);
                break;
            case HTML_CONTENT:
                FileUtils.write(file, content, Charset.defaultCharset());
                break;
            case HTML_FULL:
                FileUtils.write(file, html, Charset.defaultCharset());
                break;
            default:
                throw new IllegalArgumentException("Ambigous ExportType!");
        }
        if(next.size() > 0){
            FileUtils.forceMkdir(new File(path + "/" + suffix));
            next.stream().forEach(n -> {
                try {
                    n.writeTo(path + "/" + suffix, exportType);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<TreeNode> getNext() {
        return next;
    }

    public void setNext(List<TreeNode> next) {
        this.next = next;
    }

    public void addNext(TreeNode node){
        next.add(node);
    }
}
