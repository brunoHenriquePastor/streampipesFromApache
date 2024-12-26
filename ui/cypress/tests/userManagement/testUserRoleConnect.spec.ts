/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
import { UserRole } from '../../../src/app/_enums/user-role.enum';
import { UserUtils } from '../../support/utils/UserUtils';
import { ConnectUtils } from '../../support/utils/connect/ConnectUtils';
import { PermissionUtils } from '../../support/utils/user/PermissionUtils';
import { GeneralUtils } from '../../support/utils/GeneralUtils';

describe('Test User Roles for Connect', () => {
    var connectAdminUser;
    beforeEach('Setup Test', () => {
        cy.initStreamPipesTest();
        connectAdminUser = UserUtils.createUser(
            'user',
            UserRole.ROLE_CONNECT_ADMIN,
        );
        ConnectUtils.addMachineDataSimulator('simulator');
    });

    it('Connect admin should not see adapters of other users', () => {
        UserUtils.switchUser(connectAdminUser);

<<<<<<< HEAD
        // Login as user and check if connect is visible to user
        cy.switchUser(connect_admin);
=======
        GeneralUtils.validateAmountOfNavigationIcons(3);
>>>>>>> upstream/dev

        // Validate that no adapter is visible
        ConnectUtils.checkAmountOfAdapters(0);
    });

    it('Connect admin should see public adapters of other users', () => {
        // Set adapter to public
        PermissionUtils.markElementAsPublic();

        UserUtils.switchUser(connectAdminUser);

        GeneralUtils.validateAmountOfNavigationIcons(3);

        // Validate that adapter is visible
        ConnectUtils.checkAmountOfAdapters(1);
    });

    it('Connect admin should see shared adapters of other users', () => {
        // Share adapter with user
        PermissionUtils.authorizeUser(connectAdminUser.email);

        UserUtils.switchUser(connectAdminUser);

        GeneralUtils.validateAmountOfNavigationIcons(3);

        // Validate that adapter is visible
        ConnectUtils.checkAmountOfAdapters(1);
    });
});
