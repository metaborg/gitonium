# Gitonium Documentation
This is the documentation for Gitonium, based on the [MkDocs Material Template](https://github.com/Virtlink/mkdocs-material-template).

## Quick Start
To build the pages and see edits live using [Docker](https://www.docker.com/):

```shell
cd docs/
make
```

Or using [Python 3](https://www.python.org/), creating and activating a _virtual environment_ using `virtualenv` (the more featureful ancestor of `venv`, install with `python3 -m pip install virtualenv`):

```shell
virtualenv venv
source venv/bin/activate

cd docs/
pip install -r requirements.txt
mkdocs serve

deactivate
```

Navigate to [localhost:8000](http://localhost:8000/) to see the documentation.
The local documentation is automatically reloaded when changes occur.
Changes pushed to the `main` branch are automatically deployed to Github Pages.

## Updating Dependencies
Using the [pip-check-updates](https://pypi.org/project/pip-check-updates/) tool, you can check the versions of the dependencies. Install in a _virtual environment_:

```shell
pip install pip-check-updates
```

Usage:

```shell
cd docs/
pcu requirements.txt
```

And update the dependencies to their latest versions using:

```shell
cd docs/
pcu -u requirements.txt
```
