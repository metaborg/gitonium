FROM python:3.12-alpine

ARG DOCS=docs/
ARG PORT=8000
ARG TOOLS=tools/
ARG REQUIREMENTS=requirements.txt

RUN apk upgrade --update-cache -a \
 && apk add --no-cache \
      git \
      git-fast-import \
      openssh \
 && apk add --no-cache --virtual .build gcc musl-dev

COPY ${TOOLS} tools/
COPY ${REQUIREMENTS} requirements.txt
RUN pip install --no-cache-dir -r requirements.txt \
 && apk del .build gcc musl-dev \
 && rm -rf /tmp/* /root/.cache
RUN <<FILE cat > /root/.gitconfig
[safe]
     directory = /repo
FILE

WORKDIR /repo/${DOCS}
EXPOSE ${PORT}
ENTRYPOINT ["mkdocs"]
CMD ["serve", "--dev-addr=0.0.0.0:${PORT}"]
