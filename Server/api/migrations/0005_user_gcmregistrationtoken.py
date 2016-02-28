# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0004_auto_20160226_2041'),
    ]

    operations = [
        migrations.AddField(
            model_name='user',
            name='gcmRegistrationToken',
            field=models.TextField(blank=True, null=True),
        ),
    ]
