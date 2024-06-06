package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class HelpActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        //setupNavigation(bottomNav, R.id.menu_help);

        WebView webView = findViewById(R.id.webView);

        String markdown = loadMarkdownFromAssets();

        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        webView.loadData(html, "text/html", "UTF-8");
    }

    private String loadMarkdownFromAssets() {
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getResources().openRawResource(R.raw.help)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
        } catch (IOException e) {
            Log.e("HelpActivity", "Error reading file: " + e);
        }

        return stringBuilder.toString();
    }
}