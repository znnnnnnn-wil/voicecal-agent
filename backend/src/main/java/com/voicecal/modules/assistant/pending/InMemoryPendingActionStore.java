package com.voicecal.modules.assistant.pending;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * 基于内存 Map 的待确认操作存储实现。
 */
@Component
public class InMemoryPendingActionStore implements PendingActionStore {

    private final ConcurrentMap<String, PendingAction> actions = new ConcurrentHashMap<>();

    @Override
    public PendingAction save(PendingAction action) {
        actions.put(action.id(), action);
        return action;
    }

    @Override
    public Optional<PendingAction> findById(String id) {
        return Optional.ofNullable(actions.get(id));
    }

    @Override
    public Optional<PendingAction> findByIdAndConversationId(String id, String conversationId) {
        return findById(id)
                .filter(action -> action.conversationId().equals(conversationId));
    }

    @Override
    public void remove(String id) {
        actions.remove(id);
    }

    @Override
    public List<PendingAction> findByConversationId(String conversationId) {
        return actions.values().stream()
                .filter(action -> action.conversationId().equals(conversationId))
                .sorted(Comparator.comparing(PendingAction::createdAt))
                .toList();
    }

    @Override
    public void removeExpired() {
        LocalDateTime now = LocalDateTime.now();
        actions.values().removeIf(action -> action.isExpired(now));
    }

    @Override
    public void clear() {
        actions.clear();
    }
}
