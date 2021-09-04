/*
 * MIT License
 *
 * Copyright (c) 2017-2020 RiiConnect24 and its contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.wiilink24.bot.utils;

import net.dv8tion.jda.api.entities.Member;

import java.util.List;

/**
 * @author Artuto
 */

public class FormatUtil
{
    public static String listOfMembers(List<Member> list, String query)
    {
        StringBuilder out = new StringBuilder(":warning: Multiple members found matching \"" + query + "\":");
        for(int i = 0; i < 6 && i < list.size(); i++)
        {
            out.append("\n - ").append(list.get(i).getUser().getName()).append("#")
                    .append(list.get(i).getUser().getDiscriminator()).append(" (ID:")
                    .append(list.get(i).getUser().getId()).append(")");
        }

        if(list.size() > 6)
            out.append("\n**And ").append(list.size() - 6).append(" more...**");

        return out.toString();
    }
}