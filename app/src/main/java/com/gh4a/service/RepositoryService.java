package com.gh4a.service;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.gh4a.DefaultClient;
import com.gh4a.Gh4Application;
import com.gh4a.utils.HtmlUtils;
import com.gh4a.utils.rx.RxTools;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.StarService;
import org.eclipse.egit.github.core.service.WatcherService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import io.reactivex.Observable;

public class RepositoryService {
    public static Observable loadReadme(Activity activity, String repoOwner, String repoName, String ref) {
        Gh4Application app = (Gh4Application) activity.getApplicationContext();

        return Observable.create(emitter -> {
            Log.d("TEST", "loading Readme file from wweb NETWORK CALL");
            GitHubClient client = new DefaultClient("application/vnd.github.v3.html");
            client.setOAuth2Token(app.getAuthToken());

            ContentsService contentService = new ContentsService(client);
            try {
                String html = contentService.getReadmeHtml(new RepositoryId(repoOwner, repoName), ref);
                if (html != null) {
                    emitter.onNext(HtmlUtils.rewriteRelativeUrls(html, repoOwner, repoName, ref));
                    emitter.onComplete();
                }
            } catch (RequestException e) {
                /* don't spam logcat with 404 errors, those are normal */
                if (e.getStatus() != 404) {
                    emitter.onError(e);
                }
            } catch (IOException ioe) {
                emitter.onError(ioe);
            }
        })
        .compose(RxTools.handle(app, activity, "loadReadme"));
    }

    public static Observable loadPullRequestCount(Activity activity, Repository repository, String state) {
        Gh4Application app = (Gh4Application) activity.getApplicationContext();
        return Observable.create(emitter -> {
            final String QUERY_FORMAT = "type:pr repo:%s/%s state:%s";
            IssueService issueService = (IssueService)
                    Gh4Application.get().getService(Gh4Application.ISSUE_SERVICE);
            HashMap<String, String> filterData = new HashMap<>();
            filterData.put("q", String.format(Locale.US, QUERY_FORMAT,
                    repository.getOwner().getLogin(), repository.getName(), state));
            try {
                int count = issueService.getSearchIssueResultCount(filterData);
                Log.d("TEST", "getPullRequestCount called --> repository service: " + count);

                emitter.onNext(count);
                Log.d("TEST", "getPullRequestCount called --> repository service AFTER ON NEXT");

                emitter.onComplete();
                Log.d("TEST", "getPullRequestCount called --> repository service AFTER ON COMPLETE");

            } catch(IOException ioe) {
                Log.d("TEST", "getPullRequestCount called --> ERROR: " + ioe.toString());
                emitter.onError(ioe);
            }
        })
        .compose(RxTools.handle(app, activity, "loadPullRequestCount"));
    }

    public static Observable updateStar(Gh4Application app, Activity activity, String repoOwner, String repoName, boolean isStarring) {
        return Observable.create(e -> {
            Log.d("TEST", "updateStarringStatus creating the observable");
            StarService starService = (StarService)
                    Gh4Application.get().getService(Gh4Application.STAR_SERVICE);
            RepositoryId repoId = new RepositoryId(repoOwner, repoName);
            if (isStarring) starService.unstar(repoId);
            else starService.star(repoId);

            Log.d("TEST", "updateStarringStatus calling onNext method");
            e.onNext(!isStarring);
            e.onComplete();
        }).compose(RxTools.handleNoCache(app, activity));
    }

    public static Observable updateWatch(Gh4Application app, Activity activity, String repoOwner, String repoName, boolean isWatching) {
        return Observable.create(e -> {
            WatcherService watcherService = (WatcherService)
                    Gh4Application.get().getService(Gh4Application.WATCHER_SERVICE);
            RepositoryId repoId = new RepositoryId(repoOwner, repoName);
            if (isWatching) {
                watcherService.unwatch(repoId);
            } else {
                watcherService.watch(repoId);
            }

            e.onNext(!isWatching);
            e.onComplete();
        }).compose(RxTools.handleNoCache(app, activity));
    }

    public static Observable isStarring(Gh4Application app, Activity activity, String repoOwner, String repoName) {
        return Observable.create(e -> {
            StarService starService = (StarService)
                    Gh4Application.get().getService(Gh4Application.STAR_SERVICE);
            try {
                boolean isStarring = starService.isStarring(new RepositoryId(repoOwner, repoName));
                e.onNext(isStarring);
                e.onComplete();
            } catch(Exception ex) {
                e.onError(ex);
            }
        }).compose(RxTools.handleNoCache(app, activity));
    }


    public static Observable isWatching(Gh4Application app, Activity activity, String repoOwner, String repoName) {
        return Observable.create(e -> {
            WatcherService watcherService = (WatcherService)
                    Gh4Application.get().getService(Gh4Application.WATCHER_SERVICE);

            try {
                boolean isWatching = watcherService.isWatching(new RepositoryId(repoOwner, repoName));
                e.onNext(isWatching);
                e.onComplete();
            } catch(Exception ex) {
                e.onError(ex);
            }
        }).compose(RxTools.handleNoCache(app, activity));
    }

    public static Observable loadRepository(Activity activity, String repoOwner, String repoName) {
        Gh4Application app = (Gh4Application) activity.getApplicationContext();
        return Observable.create(e -> {
            org.eclipse.egit.github.core.service.RepositoryService repoService = (org.eclipse.egit.github.core.service.RepositoryService)
            Gh4Application.get().getService(Gh4Application.REPO_SERVICE);

            try {
                Repository repository = repoService.getRepository(repoOwner, repoName);
                e.onNext(repository);
                e.onComplete();
            } catch (Exception ex) {
                e.onError(ex);
            }
        })
        .compose(RxTools.handle(app, activity, "loadRepository"));
    }
}