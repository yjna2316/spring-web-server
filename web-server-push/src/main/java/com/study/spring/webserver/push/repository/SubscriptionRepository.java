package com.study.spring.webserver.push.repository;


import com.study.spring.webserver.push.model.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {

  Subscription findById(Long seq);

  Subscription save(Subscription user);

  Optional<Subscription> findByUserSeq(Long userSeq);

  List<Subscription> findAll();

}

