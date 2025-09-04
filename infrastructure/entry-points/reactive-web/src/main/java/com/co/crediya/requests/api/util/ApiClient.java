package com.co.crediya.requests.api.util;

import com.co.crediya.requests.model.loanapplication.Actor;

public record ApiClient(Actor actor, String jwt) {}
