package io.quarkiverse.githubapp.runtime.github;

import java.io.IOException;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.ServiceDownException;

import io.quarkus.runtime.LaunchMode;

@ApplicationScoped
public class GitHubFileDownloader {

    private static final Logger LOG = Logger.getLogger(GitHubFileDownloader.class);

    @Inject
    LaunchMode launchMode;

    @SuppressWarnings("deprecation")
    public Optional<String> getFileContent(GHRepository ghRepository, String ref, String fullPath) {
        try {
            GHContent ghContent = ghRepository.getFileContent(fullPath, ref);

            return Optional.of(ghContent.getContent());
        } catch (GHFileNotFoundException e) {
            // The config being not found can be perfectly acceptable, we log a warning in dev and test modes.
            // Note that you will have a GHFileNotFoundException if the file exists but you don't have the 'Contents' permission.
            if (launchMode.isDevOrTest()) {
                LOG.warn("Unable to read file " + fullPath + " for repository " + ghRepository.getFullName()
                        + ". Either the file does not exist or the 'Contents' permission has not been set for the application.");
            }
            return Optional.empty();
        } catch (ServiceDownException e) {
            throw new GitHubServiceDownException(
                    "Error downloading file " + fullPath + " for repository " + ghRepository.getFullName());
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Error downloading file " + fullPath + " for repository " + ghRepository.getFullName(), e);
        }
    }
}
