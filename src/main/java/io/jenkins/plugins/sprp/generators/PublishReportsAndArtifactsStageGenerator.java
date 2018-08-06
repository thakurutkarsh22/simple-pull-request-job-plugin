package io.jenkins.plugins.sprp.generators;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import io.jenkins.plugins.sprp.PipelineGenerator;
import io.jenkins.plugins.sprp.models.ArtifactPublishingConfig;
import io.jenkins.plugins.sprp.models.ReportsAndArtifactsInfo;
import org.jenkinsci.Symbol;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Extension
@Symbol("publishReportsAndArtifactsStage")
public class PublishReportsAndArtifactsStageGenerator extends PipelineGenerator<ReportsAndArtifactsInfo> {

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    @Nonnull
    @Override
    public List<String> toPipeline(ReportsAndArtifactsInfo reportsAndArtifactsInfo) {

        ArrayList<String> reports = reportsAndArtifactsInfo.getReports();
        ArtifactPublishingConfig config = reportsAndArtifactsInfo.getArtifactPublishingConfig();
        ArrayList<HashMap<String, String>> publishArtifacts = reportsAndArtifactsInfo.getPublishArtifacts();

        ArrayList<String> snippetLines = new ArrayList<>();

        if (reports == null && config == null) {
            return snippetLines;
        }

        snippetLines.add("stage('Publish reports & artifacts') {");
        snippetLines.add("steps {");

        if (reports != null) {
            snippetLines.addAll(getPublishReportSnippet(reports));
        }

        if (config != null) {
            snippetLines.add("" + "withCredentials([file(credentialsId: '" + config.getCredentialId() + "', variable: 'FILE')]) {");

            for (HashMap<String, String> artifact : publishArtifacts) {
                snippetLines.add("sh 'scp -i $FILE " + artifact.get("from") + " " + config.getUser() + "@" + config.getHost() + ":" + artifact.get("to") + "'");
            }

            snippetLines.add("}");
        }

        snippetLines.add("}");
        snippetLines.add("}");

        return snippetLines;
    }

    @Override
    public boolean canConvert(@Nonnull Object object) {
        return object instanceof PublishReportsAndArtifactsStageGenerator;
    }

    private List<String> getPublishReportSnippet(ArrayList<String> paths) {
        ArrayList<String> snippetLines = new ArrayList<>();

        for (String p : paths) {
            snippetLines.add("junit '" + p + "'");
        }

        return snippetLines;
    }
}
