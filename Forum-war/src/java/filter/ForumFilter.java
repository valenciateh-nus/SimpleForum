package filter;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import managedbean.ForumManagedBean;

/**
 *
 * @author valenciateh
 */
public class ForumFilter implements Filter {

    @Inject
    private ForumManagedBean forumManagedBean;

    public ForumFilter() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request1 = (HttpServletRequest) request;
        if (forumManagedBean == null || forumManagedBean.getId() == -1L) {
            System.out.println(request1.getContextPath());
            ((HttpServletResponse) response).sendRedirect(request1.getContextPath() + "/index.xhtml");
        } else {
            chain.doFilter(request1, response);
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
}
