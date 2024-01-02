package com.digicore.automata.data.lib.modules.common.background.services;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

public interface BackGroundService {
  void runSystemStartUpTask();

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // Run once a day
    void disableInactiveAccounts();
}
