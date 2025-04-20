package com.magic.habbittracker.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIcons {
    val iconMap: Map<String, ImageVector> = mapOf(
        "fitness" to Icons.Default.FitnessCenter,
        "run" to Icons.Default.DirectionsRun,
        "walk" to Icons.Default.DirectionsWalk,
        "bike" to Icons.Default.DirectionsBike,
        "swim" to Icons.Default.Pool,
        "sports" to Icons.Default.SportsSoccer,
        "yoga" to Icons.Default.SelfImprovement,
        "health" to Icons.Default.MonitorHeart,
        "medical" to Icons.Default.LocalHospital,
        "water" to Icons.Default.WaterDrop,
        
        "breakfast" to Icons.Default.FreeBreakfast,
        "lunch" to Icons.Default.BrunchDining,
        "dinner" to Icons.Default.DinnerDining,
        "food" to Icons.Default.Restaurant,
        "fastfood" to Icons.Default.Fastfood,
        "nosugar" to Icons.Default.NoFood,
        "grocery" to Icons.Default.LocalGroceryStore,
        "drink" to Icons.Default.LocalBar,
        "coffee" to Icons.Default.Coffee,
        
        "book" to Icons.Default.MenuBook,
        "study" to Icons.Default.School,
        "learn" to Icons.Default.Psychology,
        "code" to Icons.Default.Code,
        "create" to Icons.Default.Create,
        "write" to Icons.Default.Edit,
        "music" to Icons.Default.MusicNote,
        "art" to Icons.Default.Brush,
        "language" to Icons.Default.Language,
        "translate" to Icons.Default.Translate,
        
        "work" to Icons.Default.Work,
        "time" to Icons.Default.Schedule,
        "alarm" to Icons.Default.Alarm,
        "computer" to Icons.Default.Computer,
        "mail" to Icons.Default.Email,
        "call" to Icons.Default.Call,
        "meeting" to Icons.Default.Groups,
        "task" to Icons.Default.Task,
        "checklist" to Icons.Default.Checklist,
        "notes" to Icons.Default.Notes,
        
        "money" to Icons.Default.AttachMoney,
        "savings" to Icons.Default.Savings,
        "payments" to Icons.Default.Payments,
        "budget" to Icons.Default.AccountBalance,
        "wallet" to Icons.Default.AccountBalanceWallet,
        "shopping" to Icons.Default.ShoppingCart,
        "receipt" to Icons.Default.Receipt,
        "discount" to Icons.Default.Sell,
        "credit" to Icons.Default.CreditCard,
        
        "home" to Icons.Default.Home,
        "pet" to Icons.Default.Pets,
        "clean" to Icons.Default.CleaningServices,
        "trash" to Icons.Default.DeleteOutline,
        "laundry" to Icons.Default.LocalLaundryService,
        "garden" to Icons.Default.Yard,
        "family" to Icons.Default.People,
        "baby" to Icons.Default.ChildCare,
        "travel" to Icons.Default.Flight,
        "car" to Icons.Default.DirectionsCar,
        
        "meditation" to Icons.Default.SelfImprovement,
        "spa" to Icons.Default.Spa,
        "sleep" to Icons.Default.Bedtime,
        "notification" to Icons.Default.Notifications,
        "heart" to Icons.Default.Favorite,
        "smile" to Icons.Default.Mood,
        "star" to Icons.Default.Star,
        "check" to Icons.Default.Check,
        "done" to Icons.Default.Done,
        "no" to Icons.Default.Block
    )
    
    val allIcons: List<IconData> = iconMap.map { (name, icon) ->
        IconData(name, icon)
    }
    
    fun getIconByName(name: String?): ImageVector {
        return name?.let { iconMap[it] } ?: Icons.Default.Check
    }
    
    fun getIconName(icon: ImageVector): String? {
        return iconMap.entries.find { it.value == icon }?.key
    }
}

data class IconData(
    val name: String,
    val icon: ImageVector
)