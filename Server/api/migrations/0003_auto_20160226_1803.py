# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0002_auto_20160226_1538'),
    ]

    operations = [
        migrations.RenameField(
            model_name='user',
            old_name='rsaKey',
            new_name='aesKey',
        ),
    ]
