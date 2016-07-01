package com.teamunemployment.breadcrumbs.DisplayCrumb.Data;

/**
 * Created by jek40 on 30/06/2016.
 */
public interface CrumbRepositoryContract {

    String LoadCrumbDescription(String crumbId);
    String LoadCrumbPlaceName(String id);
}
