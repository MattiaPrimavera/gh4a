/*
 * Copyright 2011 Azwan Adli Abdullah
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gh4a.adapter;

import org.eclipse.egit.github.core.SearchUser;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.utils.GravatarUtils;
import com.gh4a.utils.StringUtils;

public class SearchUserAdapter extends RootAdapter<SearchUser> implements OnClickListener {
    private AQuery aq;
    
    public SearchUserAdapter(Context context) {
        super(context);
        aq = new AQuery(context);
    }
    
    @Override
    public View doGetView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder viewHolder = null;
        
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) LayoutInflater.from(mContext);
            v = vi.inflate(R.layout.row_gravatar_1, parent, false);

            Gh4Application app = Gh4Application.get(mContext);
            Typeface boldCondensed = app.boldCondensed;
            Typeface italic = app.italic;
            
            viewHolder = new ViewHolder();
            viewHolder.ivGravatar = (ImageView) v.findViewById(R.id.iv_gravatar);
            viewHolder.ivGravatar.setOnClickListener(this);

            viewHolder.tvTitle = (TextView) v.findViewById(R.id.tv_title);
            viewHolder.tvTitle.setTypeface(boldCondensed);
            
            viewHolder.tvExtra = (TextView) v.findViewById(R.id.tv_extra);
            viewHolder.tvExtra.setTypeface(italic);
            
            v.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final SearchUser user = mObjects.get(position);
        
        aq.recycle(v);

        viewHolder.ivGravatar.setTag(user);
        if (!StringUtils.isBlank(user.getGravatarId())) {
            aq.id(viewHolder.ivGravatar).image(GravatarUtils.getGravatarUrl(user.getGravatarId()), 
                    true, false, 0, 0, aq.getCachedImage(R.drawable.default_avatar), 0);
        }
        else {
            aq.id(viewHolder.ivGravatar).image(R.drawable.default_avatar);
        }

        viewHolder.tvTitle.setText(StringUtils.formatName(user.getLogin(), user.getName()));
        viewHolder.tvExtra.setText(mContext.getString(R.string.user_extra_data,
                user.getFollowers(), user.getPublicRepos()));
        
        return v;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_gravatar) {
            SearchUser user = (SearchUser) v.getTag();
            if (!StringUtils.isBlank(user.getLogin())) {
                Gh4Application.get(mContext).openUserInfoActivity(mContext,
                        user.getLogin(), user.getLogin());
            }
        }
    }

    private static class ViewHolder {
        public TextView tvTitle;
        public ImageView ivGravatar;
        public TextView tvExtra;
    }
}