# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0003_auto_20160226_1803'),
    ]

    operations = [
        migrations.AlterField(
            model_name='user',
            name='contacts',
            field=models.ManyToManyField(to='api.User'),
        ),
    ]
