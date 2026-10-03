package com.wolfe.catalog.pdf;

import java.nio.file.*;
import java.time.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PdfImportRecoveryScheduler {
    private static final Logger log = LoggerFactory.getLogger(PdfImportRecoveryScheduler.class);
    private final PdfImportJobRepository jobs;
    private final com.wolfe.ops.SchedulerLockService schedulerLock;
    public PdfImportRecoveryScheduler(PdfImportJobRepository jobs, com.wolfe.ops.SchedulerLockService schedulerLock) { this.jobs = jobs; this.schedulerLock = schedulerLock; }

    @Scheduled(fixedDelayString = "PT10M")
    @Transactional
    public void recoverStaleJobs() {
        schedulerLock.runIfLeader(0x574F4C4645504446L, () -> recoverStaleJobsLocked());
    }

    private void recoverStaleJobsLocked() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(1);
        List<PdfImportJob> stale = jobs.findByStatusInAndUpdatedAtBefore(List.of("QUEUED","PROCESSING","EXTRACTING","MATCHING"), cutoff);
        for (PdfImportJob candidate : stale) {
            // The initial query is only a candidate list. Re-read and lock the row immediately
            // before failing it so a concurrent worker heartbeat cannot be lost to a stale snapshot.
            PdfImportJob job = jobs.findByIdForUpdate(candidate.getId()).orElse(null);
            if (job == null || !List.of("QUEUED", "PROCESSING", "EXTRACTING", "MATCHING").contains(job.getStatus())
                    || !job.getUpdatedAt().isBefore(cutoff)) {
                continue;
            }
            job.setStatus("FAILED");
            job.setErrorMessage("PDF import interrupted by an application restart or worker timeout. Please upload again.");
            jobs.save(job);
            if (job.getFilePath() != null) { try { Files.deleteIfExists(Paths.get(job.getFilePath()).toAbsolutePath().normalize()); } catch (Exception ex) { log.warn("Unable to clean stale PDF job file {}", job.getId(), ex); } }
        }
    }
}
