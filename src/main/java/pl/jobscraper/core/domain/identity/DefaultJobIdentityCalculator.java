package pl.jobscraper.core.domain.identity;

import pl.jobscraper.core.domain.model.Job;
import java.util.Objects;
import static pl.jobscraper.core.domain.identity.NormalizationRules.*;

public class DefaultJobIdentityCalculator implements JobIdentityCalculator {

    private final HashingService hashing;

    public DefaultJobIdentityCalculator(HashingService hashing) {
        this.hashing = Objects.requireNonNull(hashing, "hashing must not be null");
    }


    @Override
    public JobIdentity calculate(Job job){
        if(job == null){
            throw new IllegalArgumentException("Job is null");
        }

        var canon = canonicalUrl(job.getUrl());
        if (canon.isPresent()) {
            return JobIdentity.fromCanonicalUrl(canon.get());
        }

        final String companyN  = normalizeCompany(job.getCompany());
        final String titleN    = normalizeTitle(job.getTitle());
        final String locationN = normalizeLocation(job.getLocation());
        final String payload = companyN + "#" + titleN + "#" + locationN;
        final String hashHex = hashing.sha256Hex(payload);

        return JobIdentity.fromFallbackHash(hashHex);


    }

}
