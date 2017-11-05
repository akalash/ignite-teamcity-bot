package org.apache.ignite.ci.web.rest;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.ignite.Ignite;
import org.apache.ignite.ci.HelperConfig;
import org.apache.ignite.ci.IgnitePersistentTeamcity;
import org.apache.ignite.ci.analysis.FullChainRunCtx;
import org.apache.ignite.ci.conf.BranchTracked;
import org.apache.ignite.ci.conf.ChainAtServerTracked;
import org.apache.ignite.ci.runners.PrintChainResults;
import org.apache.ignite.ci.tcmodel.hist.BuildRef;
import org.apache.ignite.ci.web.BackgroundUpdater;
import org.apache.ignite.ci.web.CtxListener;
import org.apache.ignite.ci.web.rest.model.current.ChainAtServerCurrentStatus;
import org.apache.ignite.ci.web.rest.model.current.FailureDetails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;

@Path(GetAllTestFailures.ALL)
@Produces(MediaType.APPLICATION_JSON)
public class GetAllTestFailures {
    public static final String ALL = "all";
    @Context
    private ServletContext context;

    @GET
    @Path("failures")
    public FailureDetails getTestFails(@Nullable @QueryParam("branch") String branchOrNull) {
        final String key = Strings.nullToEmpty(branchOrNull);
        final BackgroundUpdater updater = (BackgroundUpdater)context.getAttribute(CtxListener.UPDATER);
        return updater.get(ALL, key, this::getAllTestFailsNoCache);
    }


    @GET
    @Path("failuresNoCache")
    @NotNull public FailureDetails getAllTestFailsNoCache(@Nullable @QueryParam("branch") String branchOpt ) {
        Ignite ignite = (Ignite)context.getAttribute(CtxListener.IGNITE);
        final FailureDetails res = new FailureDetails();
        final String branch = isNullOrEmpty(branchOpt) ? "master" : branchOpt;
        final BranchTracked tracked = HelperConfig.getTrackedBranches().getBranchMandatory(branch);
        for (ChainAtServerTracked chainAtServerTracked : tracked.chains) {
            try (IgnitePersistentTeamcity teamcity = new IgnitePersistentTeamcity(ignite, chainAtServerTracked.serverId)) {
                final List<BuildRef> builds = teamcity.getFinishedBuildsIncludeSnDepFailed(
                    chainAtServerTracked.getSuiteIdMandatory(),
                    chainAtServerTracked.getBranchForRestMandatory());
                Stream<Optional<FullChainRunCtx>> stream
                    = builds.stream().parallel()
                    .filter(b -> b.getId() != null)
                    .map(build -> PrintChainResults.processChainByRef(teamcity, false, build, false));

                final Map<String, IgnitePersistentTeamcity.RunStat> map = teamcity.runTestAnalysis();
                stream.forEach(
                    pubCtx -> {
                        final ChainAtServerCurrentStatus chainStatus = new ChainAtServerCurrentStatus();
                        chainStatus.serverName = teamcity.serverId();
                        pubCtx.ifPresent(ctx -> chainStatus.initFromContext(teamcity, ctx, map));
                        res.servers.add(chainStatus);
                    }
                );

            }
        }
        return res;
    }

}