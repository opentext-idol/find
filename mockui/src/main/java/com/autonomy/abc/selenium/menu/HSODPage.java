package com.autonomy.abc.selenium.menu;

import com.autonomy.abc.selenium.page.admin.HSODevelopersPage;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.login.AbcHasLoggedIn;
import com.autonomy.abc.selenium.page.promotions.*;
import com.autonomy.abc.selenium.page.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.page.search.HSOSearchPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum HSODPage {
    LOGIN(LoginPage.class, HSOLoginPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new HSOLoginPage(driver, new AbcHasLoggedIn(driver));
        }
    },

    ANALYTICS(NavBarTabId.ANALYTICS, AnalyticsPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new AnalyticsPage(driver);
        }
    },

    SEARCH(NavBarTabId.SEARCH, SearchPage.class, HSOSearchPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new HSOSearchPage(driver);
        }
    },

    CONNECTIONS(NavBarTabId.CONNECTIONS, ConnectionsPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return ConnectionsPage.make(driver);
        }
    },
    CONNECTION_WIZARD(NewConnectionPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return NewConnectionPage.make(driver);
        }
    },
    CONNECTION_DETAILS(ConnectionsDetailPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return ConnectionsDetailPage.make(driver);
        }
    },

    INDEXES(NavBarTabId.INDEXES, IndexesPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return IndexesPage.make(driver);
        }
    },
    INDEX_WIZARD(CreateNewIndexPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return CreateNewIndexPage.make(driver);
        }
    },
    INDEX_DETAILS(IndexesDetailPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return IndexesDetailPage.make(driver);
        }
    },

    PROMOTIONS(NavBarTabId.PROMOTIONS, PromotionsPage.class, HSOPromotionsPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new HSOPromotionsPage(driver);
        }
    },
    PROMOTION_WIZARD(CreateNewPromotionsBase.class, CreateNewPromotionsPage.class, HSOCreateNewPromotionsPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new HSOCreateNewPromotionsPage(driver);
        }
    },
    PROMOTION_DETAILS(PromotionsDetailPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new PromotionsDetailPage(driver);
        }
    },
    EDIT_REFERENCES(EditDocumentReferencesPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return EditDocumentReferencesPage.make(driver);
        }
    },

    KEYWORDS(NavBarTabId.KEYWORDS, KeywordsPage.class, HSOKeywordsPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new HSOKeywordsPage(driver);
        }
    },
    KEYWORD_WIZARD(CreateNewKeywordsPage.class, HSOCreateNewPromotionsPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new HSOCreateNewPromotionsPage(driver);
        }
    },

    GETTING_STARTED(NavBarTabId.GETTING_STARTED, GettingStartedPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new GettingStartedPage(driver);
        }
    },

    DEVELOPERS(NavBarTabId.DEVELOPERS, HSODevelopersPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new HSODevelopersPage(driver);
        }
    },
    USERS(UsersPage.class, HSOUsersPage.class) {
        @Override
        protected Object safeLoad(WebDriver driver) {
            return new HSOUsersPage(driver);
        }
    };

    private final List<Class<?>> pageTypes;
    private final NavBarTabId tabId;
    private final static Map<Class<?>, HSODPage> TYPE_MAP = new HashMap<>();

    static {
        for (HSODPage page : HSODPage.values()) {
            for (Class<?> type : page.pageTypes) {
                TYPE_MAP.put(type, page);
            }
        }
    }

    HSODPage(Class<?>... types) {
        this(null, types);
    }

    HSODPage(NavBarTabId tab, Class<?>... types) {
        tabId = tab;
        pageTypes = Arrays.asList(types);
    }

    @SuppressWarnings("unchecked")
    private <T> T safeLoad(Class<T> type, WebDriver driver) {
        if (pageTypes.contains(type)) {
            return (T) safeLoad(driver);
        }
        return null;
    }

    protected abstract Object safeLoad(WebDriver driver);

    public static NavBarTabId getId(Class<?> type) {
        return TYPE_MAP.get(type).tabId;
    }

    public static <T> T load(Class<T> type, WebDriver driver) {
        return TYPE_MAP.get(type).safeLoad(type, driver);
    }
}
