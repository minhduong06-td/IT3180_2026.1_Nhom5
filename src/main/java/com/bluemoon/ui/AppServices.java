package com.bluemoon.ui;

import com.bluemoon.service.ApartmentService;
import com.bluemoon.service.AccountService;
import com.bluemoon.service.AuthService;
import com.bluemoon.service.BillingService;
import com.bluemoon.service.CollectionService;
import com.bluemoon.service.ContributionService;
import com.bluemoon.service.DashboardService;
import com.bluemoon.service.FeeService;
import com.bluemoon.service.ResidentService;

record AppServices(AuthService auth, AccountService accounts, ApartmentService apartments,
                   ResidentService residents, FeeService fees,
                   BillingService billing, CollectionService collections,
                   ContributionService contributions, DashboardService dashboard) {}
