package com.leadmapspro.service;

import com.leadmapspro.domain.AppUser;
import com.leadmapspro.domain.SearchHistory;
import com.leadmapspro.domain.SearchStatus;
import com.leadmapspro.repository.AppUserRepository;
import com.leadmapspro.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtractCompletionService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final AppUserRepository userRepository;

    public ExtractCompletionService(
            SearchHistoryRepository searchHistoryRepository, AppUserRepository userRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public int applySuccess(AppUser user, SearchHistory history, int leadsFound, int creditsCharged) {
        history.setLeadsFound(leadsFound);
        history.setCreditsCharged(creditsCharged);
        history.setStatus(SearchStatus.COMPLETED);
        searchHistoryRepository.save(history);

        int newBalance = user.getCreditBalance() - creditsCharged;
        user.setCreditBalance(newBalance);
        userRepository.save(user);
        return newBalance;
    }

    @Transactional
    public void markFailed(SearchHistory history, String message) {
        history.setStatus(SearchStatus.FAILED);
        history.setErrorMessage(message);
        searchHistoryRepository.save(history);
    }
}
