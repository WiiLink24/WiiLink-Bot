CREATE TABLE public.ticket (
    ticket_id serial NOT NULL PRIMARY KEY,
    user_id   character varying
);
CREATE TABLE public.userinfo (
    userid character varying NOT NULL PRIMARY KEY,
    strikes integer,
    afk boolean,
    afk_reason character varying
);
CREATE TABLE public.wads (
    file_id       serial PRIMARY KEY,
    filename      varchar,
    readable_name varchar
);
CREATE TABLE public.wad_urls (
    user_id varchar,
    wad_id int,
    wad_url varchar PRIMARY KEY
);