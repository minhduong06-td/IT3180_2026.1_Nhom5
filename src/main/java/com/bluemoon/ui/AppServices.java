package com.bluemoon.ui;

import com.bluemoon.service.ApartmentService;
import com.bluemoon.service.AuthService;
import com.bluemoon.service.BillingService;
import com.bluemoon.service.CollectionService;
import com.bluemoon.service.DashboardService;
import com.bluemoon.service.FeeService;

record AppServices(AuthService auth, ApartmentService apartments, FeeService fees,
                   BillingService billing, CollectionService collections, DashboardService dashboard) {}
